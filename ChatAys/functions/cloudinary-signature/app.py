from flask import Flask, request, jsonify
import hashlib
import time

app = Flask(__name__)

# 🔑 Cloudinary hesabından aldığın değerleri buraya yaz:
CLOUD_NAME = "dtbvibgjg",
API_KEY = "672958973647513",
API_SECRET = "pzrQPgeIWqAI977_Bww6zlaE4Bc"

@app.route("/get-signature", methods=["GET"])
def get_signature():
    timestamp = int(time.time())
    signature_raw = f"timestamp={timestamp}{API_SECRET}"
    signature = hashlib.sha1(signature_raw.encode()).hexdigest()
    
    return jsonify({
        "timestamp": timestamp,
        "signature": signature,
        "api_key": API_KEY,
        "cloud_name": CLOUD_NAME
    })

if __name__ == "__main__":
    app.run(debug=True)
