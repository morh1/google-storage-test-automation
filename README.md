Google Cloud Storage Test Automation<br> This project automates various Google Cloud Storage (GCS) commands (e.g., du, cat, rm, sign-url) using TestNG. 
<br>
<u>Introduction</u><br> To get started, you need a personal Google account with access to Google Cloud services. Some tests may require a service account JSON key if advanced IAM roles (like Project Creator or Storage Admin) are needed. Once configured, 
the code handles provisioning GCS resources and testing them.<br> <br>
<br>
<u>1. Prerequisites</u><br>

Google Cloud Personal Account<br> • Ensure you can enable Google Cloud APIs and manage resources.<br> <br>

gcloud Authentication<br> • Run gcloud auth login in your terminal to authenticate with your personal account.<br> <br>

Service Account JSON Key <br> • download the JSON key. Then set:<br> <code>GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json</code><br> <br>

<br>
<u>2. Setup & Run Tests</u><br>

Clone the Repo<br> <code>git clone [https://github.com/morh1/google-storage-test-automation]<br> cd google-storage-test-automation</code><br> <br>
Set GOOGLE_APPLICATION_CREDENTIALS to the path you located the json<br> 
in google-storage-test-automation/src/test/java/testcases/SignUrlCommandTest.java look for userEmail variable and change its value to your mail adress.


Run Tests<br> <code>mvn test</code><br> This will compile and execute TestNG tests, potentially creating a GCP project and performing GCS operations.<br> <br>

View Results<br> • Check the console output for success/failure.<br> • Detailed reports: <code>target/surefire-reports/</code><br>
![image](https://github.com/user-attachments/assets/b4321813-6e26-45b2-9909-105accf0abac)


<br>
<br>
<u>3. Troubleshooting</u><br>

• Billing or Permission Errors: Link a billing account to your project and ensure your Google account has correct roles.<br> • Authentication Failures: Make sure you’ve run <code>gcloud auth login</code> and your account is active (<code>gcloud auth list</code>).<br> • Service Account Issues: Confirm the <code>GOOGLE_APPLICATION_CREDENTIALS</code> path is correct and that the file exists.<br> <br>

<br>
