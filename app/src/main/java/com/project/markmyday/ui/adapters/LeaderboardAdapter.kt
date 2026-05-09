package com.project.markmyday.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.project.markmyday.R
import com.project.markmyday.data.model.QuizResult

class LeaderboardAdapter(private var results: List<QuizResult>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRank: TextView = view.findViewById(R.id.tv_rank)
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvScore: TextView = view.findViewById(R.id.tv_score)
        val cardView: CardView = view.findViewById(R.id.card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val result = results[position]
        val rank = position + 1

        holder.tvRank.text = rank.toString()
        holder.tvName.text = result.studentName

        // Privacy Logic
        if (result.isPrivacyEnabled) {
            holder.tvScore.text = holder.itemView.context.getString(R.string.privacy_placeholder)
        } else {
            holder.tvScore.text = holder.itemView.context.getString(
                R.string.score_format,
                result.score,
                result.totalQuestions
            )
        }

        // Podium UI Highlighting (Top 3)
        when (rank) {
            1 -> {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFD700")) // Gold
                holder.tvRank.text = "🥇"
            }
            2 -> {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#C0C0C0")) // Silver
                holder.tvRank.text = "🥈"
            }
            3 -> {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#CD7F32")) // Bronze
                holder.tvRank.text = "🥉"
            }
            else -> {
                holder.cardView.setCardBackgroundColor(Color.WHITE)
                holder.tvRank.text = rank.toString()
            }
        }
    }

    override fun getItemCount() = results.size

    fun updateData(newResults: List<QuizResult>) {
        this.results = newResults
        notifyDataSetChanged()
    }
}
