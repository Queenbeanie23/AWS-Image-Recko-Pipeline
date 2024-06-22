import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsV2Result;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.regions.Regions;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import java.util.List;

public class CarRegco {
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
      .withRegion(Regions.US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
      .build();

  // Amazon SQS client
  AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
      .withRegion(Regions.US_EAST_1)
      .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
      .build();


       ListObjectsV2Result result = s3.listObjectsV2(BUCKET_NAME); 
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        for (S3ObjectSummary ob : objects){
            String imageName = ob.getKey();

            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withS3Object(new S3Object().withName(imageName).withBucket(BUCKET_NAME)))
                    .withMaxLabels(10)
                    .withMinConfidence(90F);

            DetectLabelsResult resultofLabels = rekognitionClient.detectLabels(request);
            List<Label> labels = resultofLabels.getLabels();

            for(Label label :labels){
                if(label.getName().equals("Car")){
                    sqsClient.sendMessage(new SendMessageRequest().withQueueUrl(QUEUE_URL).withMessageBody(imageName));
                }
            }
        }
        sqsClient.sendMessage(new SendMessageRequest().withQueueUrl(QUEUE_URL).withMessageBody("-1"));
    }
}
    