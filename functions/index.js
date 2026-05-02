const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Triggers when a new document is added to the "notifications" collection.
 * Sends an FCM message to the appropriate topic.
 */
exports.sendNotification = functions.firestore
    .document("notifications/{notificationId}")
    .onCreate(async (snapshot, context) => {
        const data = snapshot.data();
        const messageText = data.message;
        const targetAudience = data.target_audience;

        if (!messageText) {
            console.log("No message text found, skipping notification.");
            return null;
        }

        // Determine the FCM topic based on target_audience
        // Default to topic_all if audience is "all" or unspecified
        let topic = "topic_all";
        if (targetAudience === "teachers") {
            topic = "topic_teachers";
        }

        const payload = {
            notification: {
                title: "New School Notice",
                body: messageText,
            },
            // Android-specific options to ensure high priority
            android: {
                priority: "high",
                notification: {
                    sound: "default",
                    clickAction: "FLUTTER_NOTIFICATION_CLICK", // Or your app's specific action if needed
                },
            },
            topic: topic,
        };

        try {
            const response = await admin.messaging().send(payload);
            console.log(`Successfully sent message to topic ${topic}:`, response);
            return response;
        } catch (error) {
            console.error(`Error sending message to topic ${topic}:`, error);
            throw error;
        }
    });
