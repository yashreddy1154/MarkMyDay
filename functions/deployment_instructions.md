# Deployment Instructions for Firebase Cloud Functions

Follow these steps to deploy the notification function to your Firebase project.

## Prerequisites
1.  **Firebase CLI**: Install it via npm if you haven't:
    ```bash
    npm install -g firebase-tools
    ```
2.  **Login**:
    ```bash
    firebase login
    ```

## Setup and Deployment

### 1. Initialize Firebase (if not already done)
If you haven't initialized Firebase in this project directory yet:
```bash
firebase init functions
```
- Select **"Use an existing project"** and choose your project.
- Select **"JavaScript"**.
- When asked about `package.json` and `index.js`, you can say **No** to overwriting if you want to keep the files I just created, or **Yes** and then copy the code back.
- Say **Yes** to install dependencies with npm.

### 2. Verify Files
Ensure your `functions` directory contains:
- `index.js`: The logic for sending notifications.
- `package.json`: Contains `firebase-admin` and `firebase-functions`.

### 3. Deploy
Run the following command from the project root:
```bash
firebase deploy --only functions
```

## How to Test
1.  Open the **Firebase Console**.
2.  Go to **Firestore Database**.
3.  Add a document to the `notifications` collection:
    - `message`: "Hello Teachers!"
    - `target_audience`: "teachers"
4.  Check the **Functions -> Logs** tab in the console to verify execution.
5.  Devices subscribed to `topic_teachers` should receive the notification.
