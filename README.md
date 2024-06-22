# AWS-Image-Recko-Pipeline

Overview
This project demonstrates the use of Amazon AWS cloud services to develop a distributed image recognition pipeline. The application utilizes EC2 instances, S3 storage, SQS messaging, and AWS Rekognition for machine learning-based image and text recognition.

Goals
The main objectives of this project are:

Creating virtual machines (EC2 instances) in the AWS cloud.
Utilizing cloud storage (S3) in applications.
Communicating between VMs using a queue service (SQS).
Programming distributed applications in Java on Linux VMs.
Using AWS Rekognition for machine learning.
Project Description
The image recognition pipeline is built using the following AWS services:

EC2 Instances: Two EC2 instances (A and B) with Amazon Linux AMI running Java applications.
S3 Bucket: Images are stored in the S3 bucket njit-cs-643.
SQS: Queue service used for communication between the EC2 instances.
AWS Rekognition: Service used for object and text detection in images.
Workflow
Instance A:

Reads 10 images from the S3 bucket.
Performs object detection using AWS Rekognition.
If a car is detected with confidence higher than 90%, stores the image index in SQS.
Adds an index of -1 to signal Instance B that processing is complete.
Instance B:

Reads image indexes from SQS as they become available.
Downloads corresponding images from S3.
Uses AWS Rekognition to perform text recognition.
Works in parallel with Instance A, processing images as soon as they are available.
Prints indexes of images with both cars and text, along with the actual text, to a file in its EBS.
Technologies Used
AWS EC2: Virtual machines for running Java applications.
AWS S3: Cloud storage for images.
AWS SQS: Messaging queue for inter-instance communication.
AWS Rekognition: Machine learning service for image and text recognition.
Java: Programming language used for application development.
Amazon Linux AMI: Operating system for EC2 instances.
Setup and Installation
Create EC2 Instances:

Launch two EC2 instances with Amazon Linux AMI.
Ensure both instances have the necessary IAM roles and security groups configured.
Configure S3 Bucket:

Upload images to the S3 bucket provided.
Set Up SQS Queue:

Create an SQS queue for communication between the instances.
Deploy Java Applications:

Deploy the Java applications on the respective EC2 instances.
Configure the applications to use AWS SDKs for interacting with S3, SQS, and Rekognition.
Running the Application
Start both EC2 instances.
Instance A begins processing images from S3 and sending relevant indexes to SQS.
Instance B reads from SQS, processes images for text recognition, and stores the results.
Output
A file in the EBS of Instance B containing indexes of images with both cars and text, along with the extracted text.
Conclusion
This project showcases the integration of various AWS services to build a distributed application capable of performing complex tasks like image and text recognition in the cloud.
