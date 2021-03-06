package sameAsDiagnostics;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PrintMissingTypesFreebase {
/**
 *This file is for diagnosing the approximately 1.3 million sameAs links in curated_freebase_links
 *that are missing. We will run against the freebase types folder
 *and if a freebase subject in the links file is missing a type (of any kind),
 * but present in the sameAs file, we will
 *print out the link as:
 *<idx>[tab]<fr-instance>[tab]<db-instance>[tab]"ftype-m"
 *
 * @author Mayank
 *
 */
  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{
	  
	  
	  
	  
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	
    	//the toLowercase is a precautionary measure.
    	String line = value.toString().toLowerCase();
    	String[] fields=line.split("\t");
    	//wayward line
    	if(fields.length!=3 && fields.length!=2)
    		return;
    	//came from links file
    	if(fields[0].substring(fields[0].length()-1, fields[0].length()).equals("x")){
    		context.write(new Text(fields[1]), new Text(line));
    	}else{//came from types file: send flag that it's present
    		String g=fields[0].replaceAll("/m.", "/m/");
    			context.write(new Text(g), new Text("present"));
    	}
    	
		
    	
		
    	
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
	  
	  public void reduce(Text key, Iterable<Text> values,
	                       Context context
	                       ) throws IOException, InterruptedException {
	     
	     
	    
	     HashSet<String> missing=new HashSet<String>();
	     for (Text val : values) {
	    	 
	        String k=val.toString();
	        if(k.equals("present"))
	        	return;
	        missing.add(k);
	        	
	        
	        
	        
	      }
	     String result=new String("ftype-m");
	     
	     for(String m: missing)
	    	 context.write(new Text(m), new Text(result));
	     
	     
	     
	    }
	  }


  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new String[2];
    otherArgs[0]=args[0];
    otherArgs[1]=args[1];
     /* new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length != 2) {
      System.err.println("Usage: wordcount <in> <out>");
      System.exit(2);
    }*/
    Job job = new Job(conf, "sameAs diagnostics types: freebase");
    job.setJarByClass(PrintMissingTypesFreebase.class);
    job.setMapperClass(TokenizerMapper.class);
   // job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
