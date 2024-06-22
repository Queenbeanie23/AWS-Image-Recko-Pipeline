import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.Message;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class textrecko{
    public static void main(String[] args) {
        
    
    String BUCKET_NAME = "njit-cs-643";
    String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/922217327772/ImageProcessingQueue";
  // Replace with your actual temporary credentials
  String accessKey = "####################";
  String secretKey = "########################";
  String sessionToken = " #######################################################";
  String region = "us-east-1";
  
  // Create session credentials
  AWSSessionCredentials sessionCredentials = new BasicSessionCredentials(
      accessKey, secretKey, sessionToken
  );

  // Amazon S3 client
  AmazonS3 s3 = AmazonS3ClientBuilder.standard()
      .withRegion(region)
      .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
      .build();

  // Amazon Rekognition client
  AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
      .withRegion(region)
      .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
      .build();

  // Amazon SQS client
  AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
      .withRegion(region)
      .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
      .build();

    FileWriter writer = null;

    try {
        writer = new FileWriter("/home/ec2-user/output.txt");
    } catch (IOException e) {
        e.printStackTrace();
    }

    while (true) {
        List<Message> messages = sqsClient.receiveMessage(new ReceiveMessageRequest(QUEUE_URL).withMaxNumberOfMessages(1)).getMessages();
        if (messages.isEmpty()) {
            continue;
        }
        Message message = messages.get(0);
        String imageName = message.getBody();
        
        if (imageName.equals("-1")) {
            break;
        }
        
        DetectTextRequest textRequest = new DetectTextRequest()
                .withImage(new Image().withS3Object(new S3Object().withName(imageName).withBucket(BUCKET_NAME)));
        
        DetectTextResult textResult = rekognitionClient.detectText(textRequest);
        List<TextDetection> textDetections = textResult.getTextDetections();
        
        DetectLabelsRequest labelRequest = new DetectLabelsRequest()
                .withImage(new Image().withS3Object(new S3Object().withName(imageName).withBucket(BUCKET_NAME)))
                .withMaxLabels(10)
                .withMinConfidence(90F);
        
        DetectLabelsResult labelResult = rekognitionClient.detectLabels(labelRequest);
        List<Label> labels = labelResult.getLabels();
        
        boolean hasCar = labels.stream().anyMatch(label -> label.getName().equalsIgnoreCase("Car"));
        
        if (writer != null && hasCar && !textDetections.isEmpty()) {
            try {
                for (TextDetection text : textDetections) {
                    if (text.getType().equals("WORD")) {
                        writer.write(imageName + ": " + text.getDetectedText() + " Confidence: " + text.getConfidence() + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sqsClient.deleteMessage(new DeleteMessageRequest(QUEUE_URL, message.getReceiptHandle()));
    }
   
    if (writer != null) {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }
}