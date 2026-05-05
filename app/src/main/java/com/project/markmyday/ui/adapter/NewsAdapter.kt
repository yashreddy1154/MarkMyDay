package com.project.markmyday.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.markmyday.data.model.Article
import com.project.markmyday.databinding.ItemNewsBinding
import com.project.markmyday.R

class NewsAdapter : ListAdapter<Article, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NewsViewHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvNewsTitle.text = article.title ?: binding.root.context.getString(R.string.no_title)
            binding.tvNewsDescription.text = article.description ?: binding.root.context.getString(R.string.no_description)
            binding.tvPubDate.text = article.pubDate ?: ""

            Glide.with(binding.ivNewsImage.context)
                .load(article.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .into(binding.ivNewsImage)

            binding.root.setOnClickListener {
                article.link?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }

    class NewsDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.link == newItem.link
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}
