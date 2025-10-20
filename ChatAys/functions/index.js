const functions = require("firebase-functions");
const admin = require("firebase-admin");
const cloudinary = require("cloudinary").v2;

admin.initializeApp();

// 🔹 Cloudinary yapılandırması
cloudinary.config({
  cloud_name: "dtbvibgjg",             // Senin cloud name
  api_key: "672958973647513",          // Senin API key
  api_secret: "pzrQPgeIWqAI977_Bww6zlaE4Bc",     // Cloudinary Dashboard → API Secret
});

// 🔹 Cloud Function: Signature üretir
exports.getSignature = functions.https.onCall((data, context) => {
  try {
    const timestamp = Math.floor(Date.now() / 1000);

    const signature = cloudinary.utils.api_sign_request(
      {
        timestamp: timestamp,
        upload_preset: "chat_app_upload", // Senin unsigned preset adın
      },
      cloudinary.config().api_secret
    );

    return { signature, timestamp };
  } catch (error) {
    console.error("Signature oluşturulamadı:", error);
    throw new functions.https.HttpsError("internal", "Signature oluşturulamadı");
  }
});
