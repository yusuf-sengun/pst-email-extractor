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
            lowerDate= new SimpleDateFormat("dd/MM/yyyy").parse(args[2]);
            upperDate=new SimpleDateFormat("dd/MM/yyyy").parse(args[3]);
            System.out.println("Lower Date"+lowerDate);
            System.out.println("Upper Date"+upperDate);
        }


        readCsvFile(csvFilePath);

        PSTFile pstFile = new PSTFile(pstFilePath);
        processFolder(pstFile.getRootFolder());

        for (int i=0;i<emailList.size();i++){
            System.out.println(emailList.get(i));
        }
        System.out.println(emailList.size());
        List<String> emailListWithoutDuplicates = emailList.stream().distinct().collect(Collectors.toList());
        System.out.println(emailListWithoutDuplicates.size());

    }
    public static void readCsvFile(String csvFilePath) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(csvFilePath));
        while (sc.hasNext())
        {
            String email = sc.next();
            email=email.replaceAll("\"","");
            if(checkEmailIsTelenity(email)&&checkEmailFormat(email)) {
                //System.out.println(email);
                emailList.add(email);
            }
        }
        sc.close();
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
                            emailList.add(email.getSenderEmailAddress()+"From Time COndtion ->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                        }
                    }
                    else {
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
}