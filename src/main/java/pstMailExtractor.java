import com.pff.*;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class pstMailExtractor {
    public static ArrayList<String> emailList = new ArrayList<>();
    public static boolean IsTimeParametersPassed = false;
    public static Date lowerDate;
    public static Date upperDate;

    public static DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",Locale.US);

    public static  void main (String args[]) throws IOException, PSTException, ParseException {
        String pstFilePath=args[0];
        String csvFilePath=args[1];

        if(args.length>3){
            IsTimeParametersPassed =true;
            lowerDate= new SimpleDateFormat("yyyy-MM-dd").parse(args[2]);
            upperDate=new SimpleDateFormat("yyyy-MM-dd").parse(args[3]);
            System.out.println("Lower Date"+lowerDate);
            System.out.println("Upper Date"+upperDate);
        }


        readCsvFile(csvFilePath);

        PSTFile pstFile = new PSTFile(pstFilePath);
        processFolder(pstFile.getRootFolder());

        for (int i=0;i<emailList.size();i++){
            System.out.println(emailList.get(i));
        }
        List<String> emailListWithoutDuplicates = emailList.stream().distinct().collect(Collectors.toList());
        System.out.println("---------"+emailListWithoutDuplicates.size()+" mails extracted --------------");
        writeEmailsToCSV(emailListWithoutDuplicates);

    }



    public static void readCsvFile(String csvFilePath) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(csvFilePath));
        while (sc.hasNext())
        {
            String email = sc.next();
            email=email.replaceAll("\"","");
            if(checkEmailIsTelenity(email)&&checkEmailFormat(email)) {
                if(checkMoreThanOneEmail(email)){
                    List<String> tempEmailList = preProcessMoreThanOneEmail(email);
                    for(int i=0;i<tempEmailList.size();i++){
                        System.out.println(tempEmailList.get(i));
                        emailList.add(tempEmailList.get(i));
                    }
                }
                else{
                System.out.println(email);
                emailList.add(email);
                 }
            }
        }
        sc.close();
    }

    private static ArrayList<String> preProcessMoreThanOneEmail(String email) {
        String[] tempEmailList = email.split(";");
        ArrayList<String> emailList = new ArrayList<>();
        for(int i=0;i<tempEmailList.length;i++){
            if(checkEmailFormat(tempEmailList[i])) {
                emailList.add(tempEmailList[i]);
            }
        }

        return emailList;
    }

    private static boolean checkMoreThanOneEmail(String email) {
        if(email.contains(";")){
            return true;
        }
        return false;
    }

    public static void processFolder(PSTFolder folder)
            throws PSTException, java.io.IOException
    {

        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(childFolder);
            }
        }

        if (folder.getContentCount() > 0) {
            PSTMessage email = (PSTMessage)folder.getNextChild();
            while (email != null) {
                if((checkEmailFormat(email.getSenderEmailAddress()) && checkEmailIsTelenity(email.getSenderEmailAddress()))) {
                    if (IsTimeParametersPassed){
                        if(lowerDate.before(email.getLastModificationTime()) && upperDate.after(email.getLastModificationTime()))
                        {
                            System.out.println(email.getSenderEmailAddress());
                            emailList.add(email.getSenderEmailAddress());
                        }
                    }
                    else {
                        System.out.println(email.getSenderEmailAddress());
                        emailList.add(email.getSenderEmailAddress());
                    }
                }
                email = (PSTMessage)folder.getNextChild();
            }
        }
    }
    public static boolean checkEmailFormat(String emailSenderEmailAddress){

        if (emailSenderEmailAddress.isEmpty() || emailSenderEmailAddress.startsWith("/O") || emailSenderEmailAddress.startsWith("/o") || !emailSenderEmailAddress.contains("@")){
            return false;
        }
        return true;
    }
    public static boolean checkEmailIsTelenity(String emailSenderEmailAddress){
        if ( emailSenderEmailAddress.contains("telenity")){
            return false;
        }
        return true;
    }
    private static void writeEmailsToCSV(List<String> emailListWithoutDuplicates) throws IOException {
        FileWriter writer = new FileWriter("extractedMails.csv");
        String collect = emailListWithoutDuplicates.stream().collect(Collectors.joining("\n"));
        writer.write(collect);
        writer.close();
    }
}