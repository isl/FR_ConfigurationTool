/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PathValidator;

import java.util.Stack;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import tools.WriteFile;

/**
 *
 * @author Christos
 */
public class Syntax4 {

    public static Stack read_variables(String varname) {

        Stack<String> ReadFileStack = new Stack<String>();

        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream("src/PathValidator/var.conf");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                //System.out.println (strLine);
                ReadFileStack.push(strLine);

            }
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }


        Stack<String> variableStack = new Stack<String>();
        boolean conFlag = false;
        int i = 0;

        String startValue = "==========" + varname + "_START==========";
        String endValue = "==========" + varname + "_END==========";

        while (i < ReadFileStack.size()) {

            //elegxos gia to ean teleiosan ta pedia me tis metablites gia ton links
            if (endValue.equals(ReadFileStack.elementAt(i))) {
                conFlag = false;
            }


            // eisagogi metabliton sxetika me links
            if (conFlag == true) {
                variableStack.push(ReadFileStack.elementAt(i));
            }


            //elegxos gia to ean briskomai mesa sta pedia me tis metablites gia ta links
            if (startValue.equals(ReadFileStack.elementAt(i))) {
                conFlag = true;
            }

            i++;
        }

        //System.out.println(ReadFileStack);
        //System.out.println("variableStack  "+variableStack);
        ReadFileStack.removeAllElements();
        //System.out.println(ReadFileStack);

        return variableStack;
    }

    public static void check(String str) {

        String constart = (String) read_variables("CONNSTART").elementAt(0);
        String conend = (String) read_variables("CONNEND").elementAt(0);


        char[] ar = new char[str.length()];
        ar = str.toCharArray();


        Stack<Integer> lnSrtStack = new Stack<Integer>(); //gia na entopizoume ti thesi toy link start
        Stack<Integer> lnEndStack = new Stack<Integer>(); //gia na entopizoume ti thesi toy link end

        int k = 0; //gia tin sarosi tou char array
        int linkStart = 0; //gia na entopizoume ti thesi toy link start
        int linkEnd = 0; //gia na entopizoume ti thesi toy link end
        boolean linkFlag = false; //flag pou ma boithaei ston entopismo tou link start kai end

        while (k < ar.length) {
            /*
             * Xrisimopoioume ena xexoristo int gia na min epireazontai ta 
             * apotelesmata apo tin suntomi allagi tou deiktei k (sto proto if 
             * ginetai i allagi tou)
             */
            int i = k;

            //elegxoume an exoume arxisei kai deutero link xoris na exoume kleisei to proto
            if (linkFlag == true && (ar[i] == '-') && (ar[i + 1] != '>')) {
                System.out.println("False link start at position " + i + ".Expected to close the previous first at position " + linkStart);
                break;
            }

            //elegxoume an exoume kleisi kai deutero link xoris na exoume arxisei kanourgio
            if (linkFlag == false && (ar[i] == '-') && (ar[i + 1] == '>')) {
                System.out.println("False link end at position " + i + ".Expected to start a link before try to close it!");
                break;
            }

            //elegxoume pou arxizei ena link
            if (linkFlag == false && (ar[i] == '-') && (ar[i + 1] != '>')) {
                linkStart = i + 1;
                lnSrtStack.addElement(i + 1);
                System.out.println("start " + linkStart);
                linkFlag = true;
                /*
                 * Ean to entopisoume prosthetoume 3 gia na sunexisei ton elegxo 
                 * parakato giati allios tha entopisei to -> tou --> os 
                 * xexoristo stoixei kai tha to theoreisei os link end 
                 */
                k = k + 1;
            } else {
                /*
                 * Ean den entopisoume link start tote sunexizoume kanonika ton 
                 * elegxo tou char array
                 */
                k++;
            }

            //elegxoume pou teleionei ena link
            if (linkFlag == true && (ar[i] == '-') && (ar[i + 1] == '>')) {
                linkEnd = i;
                lnEndStack.addElement(i);
                System.out.println("end " + linkEnd);
                linkFlag = false;
            }



        }


        Stack<String> result = new Stack<String>();

        String sbj = str.substring(0, lnSrtStack.get(0) - 1);
        result.push(sbj);
        String prd = str.substring(lnSrtStack.get(0), lnEndStack.get(0));
        result.push(prd);
        String obj = str.substring(lnEndStack.get(0) + 2, str.length());
        result.push(obj);

        System.out.println("Subject:  " + sbj);
        System.out.println("Predicate:  " + prd);
        System.out.println("Objetc:  " + obj);


    }

    /*
     * Tester!!!!
     */
    public static void tester(String str) {

        String[] cs = colon_seperator(str);
        //System.out.println(str);

        //System.out.println(str.replaceAll("[{}]", ""));
        System.out.println(cs[1]);
        or_seperator(cs[1]);

    }

//=================================================================================//
//#################################################################################//
//=================================================================================//
//#################################################################################//
//=================================================================================//
    /*
     * Clear the white characters (white spaces etc)
     */
    public static String clear_whiteChar(String inputStr) {

        String clearStr = inputStr.replaceAll(" OR ", "@@@OR@@@");

        clearStr = clearStr.replaceAll("\\s", "");

        clearStr = clearStr.replaceAll("@@@OR@@@", " OR ");


        return clearStr;

    }

    /*
     * Seperate the ORs
     */
    public static String[] or_seperator(String str) {

        /* Remove all special characters
         */
        //String clearStr = str.replaceAll("[{}\t]", "");

        /* Seperate string 
         */
        String[] orSep = null;
        //orSep = clearStr.split(" OR ");
        orSep = str.split(" OR ");

        return orSep;

    }

    /*
     * Seperate the input string at the first "colon" character
     */
    //public static void colon_seperator(String str) {
    public static String[] colon_seperator(String str) {

        /*
         * Split the overall string into markets
         */
        String[] overallStr = null;
        overallStr = str.split("[:]", 2);

        //for (int i=0;i<overallStr.length;i++) {
        //System.out.println(overallStr[i]);
        //}

        return overallStr;

    }

    /*
     * Seperates every single path
     */
    public static Stack<String> paths_seperator_1(String str) {
        //System.out.println("Input: "+str);
        final Stack<String> allPaths = new Stack<String>();
        allPaths.push(str);

        int j = 0;
        while (j < allPaths.size()) {

            /* Check if the given element contains more paths by checking if
             * there is the colon character
             */
            if (allPaths.elementAt(j).contains(":")) {

                /* Split the string in two parts at colon character
                 */
                String[] parts = colon_seperator(allPaths.elementAt(j));

                /* Take the 2nd part
                 */
                String secondPart = parts[1];

                /* Check if there is more paths inside the part
                 */
                if (secondPart.startsWith("{")) {

                    // If there is...
                    //System.out.println(secondPart);
                    //System.out.println(secondPart.length());
                    //System.out.println(secondPart.length()-1);
                    //System.out.println(secondPart.lastIndexOf("}"));
                    // We are going to keep only this level and the deeper ones
                    secondPart = secondPart.substring(1, (secondPart.length() - 1));

                    /* We are using a temp Sting variable because we don't want to 
                     * affect the deeper paths. So, we are locating all the subpaths 
                     * finding the '{' and '}' (if there is a subpath, it will be 
                     * inside hooks).
                     */

                    // Stacks to save the start and the end position of subpaths
                    Stack<Integer> startPositionStack = new Stack<Integer>();
                    Stack<Integer> endPositionStack = new Stack<Integer>();

                    // Convert string to char array
                    char[] ar = new char[secondPart.length()];
                    ar = secondPart.toCharArray();

                    // Hooks' counter
                    int cnt = 0;

                    // First hook flag
                    boolean frstFlag = false;

                    for (int i = 0; i < ar.length; i++) {

                        // Check if the hook is the first one and hold the position
                        if (ar[i] == '{' && cnt == 0) {
                            startPositionStack.push(i);
                        }

                        // Counting the opening hooks increasing the counter
                        if (ar[i] == '{') {
                            cnt++;
                            frstFlag = true;
                        }

                        // Counting the closing hooks decreasing the counter
                        if (ar[i] == '}') {
                            cnt--;
                        }

                        // Check if all the hooks has closed
                        if (cnt == 0 && frstFlag == true) {
                            // end point is i+1 because we want to take also the closing hook
                            endPositionStack.push(i + 1);
                            frstFlag = false;
                        }
                    }

                    Stack<String> subPaths = new Stack();
                    Stack<String> tempStrings = new Stack();

                    // Save the subpaths and create temporally string to replace them
                    for (int i = 0; i < startPositionStack.size(); i++) {
                        subPaths.push(secondPart.substring(startPositionStack.elementAt(i), endPositionStack.elementAt(i)));
                        tempStrings.push("TEMP_STRING_" + i);
                        //System.out.println(subPaths.elementAt(i));
                    }

                    // Replace the subpaths by the temporally strings
                    for (int i = 0; i < subPaths.size(); i++) {
                        secondPart = secondPart.replace(subPaths.elementAt(i), tempStrings.elementAt(i));
                        //System.out.println(secondPart);
                    }

                    // Seperate the OR values
                    String orSep[] = or_seperator(secondPart);

                    // Remove the specific element wich hase the ':'
                    allPaths.remove(j);

                    for (int i = 0; i < orSep.length; i++) {
                        // Replace back the subpaths by the temporally strings
                        for (int k = 0; k < startPositionStack.size(); k++) {
                            if (orSep[i].contains(tempStrings.elementAt(k))) {
                                orSep[i] = orSep[i].replace(tempStrings.elementAt(k), subPaths.elementAt(k));
                            }
                        }
                        // Add subpaths to the stack
                        allPaths.push(parts[0] + "|" + orSep[i]);
                    }


                } else {
                    // If there is not subpaths...

                    // Clear the stack
                    allPaths.removeAllElements();
                    // Add the only one path
                    allPaths.push(parts[0] + "|" + parts[1]);
                }

            } else {
                // There is not colon so the check can move on
                j++;
            }

        }


        return allPaths;

    }

    /*
     * Seperates every single path
     */
    public static Stack<String> paths_seperator(String str) {
        //System.out.println("Input: "+str);
        final Stack<String> allPaths = new Stack<String>();
        allPaths.push(str);

        int j = 0;
        //System.out.println("\n\n"+allPaths.size()+"\n\n");

        while (j < allPaths.size()) {

            /* Check if the given element contains more paths by checking if
             * there is the colon character
             */
            if (allPaths.elementAt(j).contains(":")) {

                /* Split the string in two parts at colon character
                 */
                String[] parts = colon_seperator(allPaths.elementAt(j));

                /* Take the 2nd part
                 */
                String secondPart = parts[1];

                /* Check if there is more paths inside the part
                 */
                if (secondPart.startsWith("{")) {

                    // If there is...
                    //System.out.println(secondPart);
                    //System.out.println(secondPart.length());
                    //System.out.println(secondPart.length()-1);
                    //System.out.println(secondPart.lastIndexOf("}"));
                    // We are going to keep only this level and the deeper ones
                    secondPart = secondPart.substring(1, (secondPart.length() - 1));

                    /* We are using a temp Sting variable because we don't want to 
                     * affect the deeper paths. So, we are locating all the subpaths 
                     * finding the '{' and '}' (if there is a subpath, it will be 
                     * inside hooks).
                     */


                    // Stacks to save the subpaths and the temp strings
                    Stack<String> subPaths = new Stack();
                    Stack<String> tempStrings = new Stack();
                    int tmpcnt = 0;

                    System.out.println("hhhhhh");

                    while (secondPart.contains("{")) {
                        System.out.println("tttt");
                        int strPos = 0, endPos = 0;

                        // Convert string to char array
                        char[] ar = new char[secondPart.length()];
                        ar = secondPart.toCharArray();

                        // Hooks' counter
                        int cnt = 0;

                        // First hook flag
                        boolean frstFlag = false;

                        for (int i = 0; i < ar.length; i++) {

                            // Check if the hook is the first one and hold the position
                            if (ar[i] == '{' && cnt == 0) {
                                //startPositionStack.push(i);
                                strPos = i;
                            }

                            // Counting the opening hooks increasing the counter
                            if (ar[i] == '{') {
                                cnt++;
                                frstFlag = true;
                            }

                            // Counting the closing hooks decreasing the counter
                            if (ar[i] == '}') {
                                cnt--;
                            }

                            // Check if all the hooks has closed
                            if (cnt == 0 && frstFlag == true) {
                                // end point is i+1 because we want to take also the closing hook
                                //endPositionStack.push(i+1);
                                endPos = i + 1;

                                subPaths.push(secondPart.substring(strPos, endPos));
                                tempStrings.push("TEMP_STRING_" + tmpcnt);
                                tmpcnt++;

                                secondPart = secondPart.replace(subPaths.peek(), tempStrings.peek());

                                //frstFlag = false;
                                break;
                            }
                        }
                    }

                    // Seperate the OR values
                    String orSep[] = or_seperator(secondPart);

                    // Remove the specific element which hase the ':'
                    allPaths.remove(j);

                    for (int i = 0; i < orSep.length; i++) {
                        // Replace back the subpaths by the temporally strings (the replacement is reversal)
                        //for (int k=0;k<subPaths.size();k++) {
                        for (int k = subPaths.size() - 1; k >= 0; k--) {
                            if (orSep[i].contains(tempStrings.elementAt(k))) {
                                orSep[i] = orSep[i].replace(tempStrings.elementAt(k), subPaths.elementAt(k));
                            }
                        }
                        // Add subpaths to the stack
                        allPaths.push(parts[0] + "|" + orSep[i]);
                    }


                } else {
                    // If there is not subpaths...

                    // Clear the stack
                    allPaths.removeAllElements();
                    // Add the only one path
                    allPaths.push(parts[0] + "|" + parts[1]);
                }

            } else {
                // There is not colon so the check can move on
                j++;
            }

        }


        return allPaths;

    }

    public static Stack<String> paths_seperator_firstTripleCheck(Stack<String> paths) {

        /* Take the first path. The choose is random because all the paths are 
         * going to have the same first triple (or set of triple)
         */
        String tempStr = paths.elementAt(0);

        /* Create a empty string table, which will be used for the results of 
         * split command
         */
        String[] tempSplitTable = null;
        tempSplitTable = tempStr.split("[|]");

        /* Take the first value of the table
         */
        String firstTripleSet = tempSplitTable[0];

        Stack<String> stackPathSeperator = new Stack();

        if (firstTripleSet.startsWith("{")) {
            // We are going to keep only this level and the deeper ones
            firstTripleSet = firstTripleSet.substring(1, (firstTripleSet.length() - 1));

            /* We are using a temp Sting variable because we don't want to 
             * affect the deeper paths. So, we are locating all the subpaths 
             * finding the '{' and '}' (if there is a subpath, it will be 
             * inside hooks).
             */


            // Stacks to save the subpaths and the temp strings
            Stack<String> subPaths = new Stack();
            Stack<String> tempStrings = new Stack();
            int tmpcnt = 0;

            while (firstTripleSet.contains("{")) {

                int strPos = 0, endPos = 0;

                // Convert string to char array
                char[] ar = new char[firstTripleSet.length()];
                ar = firstTripleSet.toCharArray();

                // Hooks' counter
                int cnt = 0;

                // First hook flag
                boolean frstFlag = false;

                for (int i = 0; i < ar.length; i++) {

                    // Check if the hook is the first one and hold the position
                    if (ar[i] == '{' && cnt == 0) {
                        //startPositionStack.push(i);
                        strPos = i;
                    }

                    // Counting the opening hooks increasing the counter
                    if (ar[i] == '{') {
                        cnt++;
                        frstFlag = true;
                    }

                    // Counting the closing hooks decreasing the counter
                    if (ar[i] == '}') {
                        cnt--;
                    }

                    // Check if all the hooks has closed
                    if (cnt == 0 && frstFlag == true) {
                        // end point is i+1 because we want to take also the closing hook
                        //endPositionStack.push(i+1);
                        endPos = i + 1;

                        subPaths.push(firstTripleSet.substring(strPos, endPos));
                        tempStrings.push("TEMP_STRING_" + tmpcnt);
                        tmpcnt++;

                        firstTripleSet = firstTripleSet.replace(subPaths.peek(), tempStrings.peek());

                        //frstFlag = false;
                        break;
                    }
                }
            }

            // Seperate the OR values
            String orSep[] = or_seperator(firstTripleSet);

            // Remove the specific element which hase the ':'
            // allPaths.remove(j);

            for (int i = 0; i < orSep.length; i++) {
                // Replace back the subpaths by the temporally strings (the replacement is reversal)
                //for (int k=0;k<subPaths.size();k++) {
                for (int k = subPaths.size() - 1; k >= 0; k--) {
                    if (orSep[i].contains(tempStrings.elementAt(k))) {
                        orSep[i] = orSep[i].replace(tempStrings.elementAt(k), subPaths.elementAt(k));
                    }
                }

            }


            /*  Replace 
             */
            for (int j = 0; j < orSep.length; j++) {
                for (int i = 0; i < paths.size(); i++) {
                    /* A temp string for paths
                     */
                    String tmp = paths.elementAt(i);
                    /* Locate the first | , this will isolate the first tiple (group of triples) for the first set
                     */
                    String firstTriple = paths.elementAt(i).substring(0, paths.elementAt(i).indexOf("|"));
                    /* Replacing the set of triples by each seperate triple of the first set
                     */
                    tmp = tmp.replace(firstTriple, orSep[j]);
                    /* Push the temp string into a stack
                     */
                    stackPathSeperator.push(tmp);
                }
            }

        } else {
            /* If the first part is a solo triple and not a set of triples, return the
             * same stack
             */
            for (int i = 0; i < paths.size(); i++) {
                stackPathSeperator.push(paths.elementAt(i));
            }
        }


        for (int i = 0; i < stackPathSeperator.size(); i++) {
            System.out.println(stackPathSeperator.elementAt(i));
        }

        return stackPathSeperator;
    }

    public static void test_OR(Stack<String> paths) {

        // Create a stack to save the paths
        Stack<String> allPaths = new Stack();

        // 
        String reg = ".*\\sOR\\s.*";


        for (int j = 0; j < paths.size(); j++) {

            // Isolating the each path from the stack 
            String str = paths.elementAt(j);

            /*
             * Check if there is OR inside the paths
             */
            if (str.matches(reg)) {

                //System.out.println(str);  

                // Stacks to save the start and the end position of subpaths
                Stack<Integer> startPositionStack = new Stack<Integer>();
                Stack<Integer> endPositionStack = new Stack<Integer>();

                // Convert string to char array
                char[] ar = new char[str.length()];
                ar = str.toCharArray();

                // Hooks' counter
                int cnt = 0;

                // First hook flag
                boolean frstFlag = false;

                for (int i = 0; i < ar.length; i++) {

                    // Check if the hook is the first one and hold the position
                    if (ar[i] == '{' && cnt == 0) {
                        startPositionStack.push(i);
                    }

                    // Counting the opening hooks increasing the counter
                    if (ar[i] == '{') {
                        cnt++;
                        frstFlag = true;
                    }

                    // Counting the closing hooks decreasing the counter
                    if (ar[i] == '}') {
                        cnt--;
                    }

                    // Check if all the hooks has closed
                    if (cnt == 0 && frstFlag == true) {
                        // end point is i+1 because we want to take also the closing hook
                        endPositionStack.push(i + 1);
                        frstFlag = false;
                    }
                }

                System.out.println("\n string at position: " + j);
                //System.out.println(startPositionStack);
                //System.out.println(endPositionStack);

                for (int k = startPositionStack.size() - 1; k >= 0; k--) {
                    //for (int k=0; k<startPositionStack.size();k++){
                    //System.out.println(startPositionStack.elementAt(k));
                    //System.out.println(endPositionStack.elementAt(k));

                    String subStr = str.substring(startPositionStack.elementAt(k), endPositionStack.elementAt(k));
                    str = str.replace(subStr, "TEMP");
                    //System.out.println(str);

                    String[] sepOr = subStr.split(" OR ");

                    for (int l = 0; l < sepOr.length; l++) {
                        //System.out.println(sepOr[l]);
                        //System.out.println(str.replace("TEMP", sepOr[l]));
                        allPaths.push(str.replace("TEMP", sepOr[l]));
                        //System.out.println(allPaths.elementAt(l));
                    }
                    str = str.replace("TEMP", subStr);
                    sepOr = null;

                }



            } else {
                /* If there is not OR inside the path, it saved at the stack 
                 * without any other processing
                 */
                allPaths.push(str);
            }

        }

        for (int i = 0; i < allPaths.size(); i++) {
            //System.out.println(allPaths.elementAt(i));
        }

    }

    /*
     * Seperate the paths in to single triples
     */
    public static Stack<String> absolute_path_seperator(Stack<String> paths) {

        // Create a stack to save the paths
        //Stack<String> allPaths = new Stack();
        Stack<String> tempPath = new Stack();

        // Create the Regex for OR
        String reg = ".*\\sOR\\s.*";

        // loop for all paths 
        for (int i = 0; i < paths.size(); i++) {

            // Isolating the each path from the stack 
            String strPath = paths.elementAt(i);
            //System.out.println(str);
            String[] sepTrip = strPath.split("[|]");

            // Loop for the split results
            for (int j = 0; j < sepTrip.length; j++) {

                // A temp string value for the paths
                String str = sepTrip[j];

                // Check if there is in the path OR
                if (str.matches(reg)) {
                    int starthook = str.indexOf("{");
                    int endhook = str.indexOf("}");

                    // Isolating the predicates with OR
                    String subStr = str.substring(starthook, endhook + 1);
                    str = str.replace(subStr, "_T_E_M_P_");

                    // Clear the string from hooks and split to the OR
                    String[] sepOr = subStr.replaceAll("[{}]", "").split(" OR ");

                    // Putting the paths into the stack with paths
                    for (int l = 0; l < sepOr.length; l++) {
                        tempPath.push(str.replace("_T_E_M_P_", sepOr[l]));
                    }
                } else {
                    tempPath.push(sepTrip[j]);
                }
            }
            tempPath.push("==");

        }

        //for (int i=0;i<tempPath.size();i++){
        //System.out.println(paths.elementAt(i));
        //System.out.println(tempPath.elementAt(i));
        //}


        return tempPath;


    }

    /*
     * Remove the weights from the triples
     */
    public static Stack<String> remove_weights_from_triples(Stack<String> triplesWithWeights) {

        Stack<String> triplesWithOUTWeights = new Stack();

        for (int i = 0; i < triplesWithWeights.size(); i++) {

            String tempTriplet = triplesWithWeights.elementAt(i);

            if (!tempTriplet.contains("==")) {
                /* Remove the all the useless characters
                 */
                tempTriplet = tempTriplet.replaceAll("[()]", "");


                /* Searching for the triples that contains weights and clear
                 * them from weights
                 */
                String reg = ".*\\[.*\\].*";

                if (tempTriplet.matches(reg)) {
                    tempTriplet = tempTriplet.replaceAll("\\[.*\\]", "");
                }
            }

            /* Save the cleared from weights triplet
             */
            triplesWithOUTWeights.push(tempTriplet);
        }


        return triplesWithOUTWeights;
    }

    /* 
     * Method for checking parentheses, hooks and brackets
     */
    public static boolean check_open_close(String str) throws IOException {

        char[] ar = new char[str.length()];
        ar = str.toCharArray();
        Stack<Character> charStack = new Stack<Character>();
        Stack<Integer> positionStack = new Stack<Integer>();
        boolean errorFlag = false;

        int i = 0;
        while (i < ar.length) {
            // Check if the character is the requested one and add it in the stack
            if ((ar[i] == '(') || (ar[i] == '[') || (ar[i] == '{')) {
                charStack.addElement(ar[i]);
                positionStack.addElement(i);
            }

            // Check if the character is the ) and remove the respective one from the stack
            if (ar[i] == ')') {
                // Check if the stack is empty. for not taking an Empty Stack Exception
                boolean flag = false;
                if (charStack.empty()) {
                    charStack.push('`'); // a random rare character
                    positionStack.push(999999999); // a random huge number
                    flag = true;
                }

                // Remove the respective of show the Error
                if (charStack.peek() == '(') {
                    charStack.remove(charStack.peek());
                    positionStack.remove(positionStack.peek());
                } else {
                    String output = "An orphan \")\" was found at position " + i+"!\n";
                    output+="If you forgot to open it, please do so.\n";
                    output+="Otherwise delete it!";
                    System.out.println(output);
                    WriteFile.write_general(output, "./src/results/OUTPUT",true);
                    errorFlag = true;
                }

                // Restore to the empty stacks (initial state of this if)
                if (flag == true) {
                    charStack.remove(charStack.peek());
                    positionStack.remove(positionStack.peek());
                    flag = false;
                }

            }

            // Check if the character is the ] and remove the respective one from the stack
            if (ar[i] == ']') {
                // Check if the stack is empty. for not taking an Empty Stack Exception
                boolean flag = false;
                if (charStack.empty()) {
                    charStack.push('`'); // a random rare character
                    positionStack.push(999999999); // a random huge number
                    flag = true;
                }

                // Remove the respective of show the Error
                if (charStack.peek() == '[') {
                    charStack.remove(charStack.peek());
                    positionStack.remove(positionStack.peek());
                } else {
                    String output = "An orphan \"]\" was found at position " + i+"!\n";
                    output+="If you forgot to open it, please do so.\n";
                    output+="Otherwise delete it!";
                    System.out.println(output);
                    WriteFile.write_general(output, "./src/results/OUTPUT",true);
                    errorFlag = true;
                }

                // Restore to the empty stacks (initial state of this if)
                if (flag == true) {
                    charStack.remove(charStack.peek());
                    positionStack.remove(positionStack.peek());
                    flag = false;
                }

            }

            // Check if the character is the } and remove the respective one from the stack
            if (ar[i] == '}') {
                // Check if the stack is empty. for not taking an Empty Stack Exception
                boolean flag = false;
                if (charStack.empty()) {
                    charStack.push('`'); // a random rare character
                    positionStack.push(999999999); // a random huge number
                    flag = true;
                }

                // Remove the respective of show the Error
                if (charStack.peek() == '{') {
                    charStack.remove(charStack.peek());
                    positionStack.remove(positionStack.peek());
                } else {
                    String output = "An orphan \"}\" was found at position " + i+"!\n";
                     output+="If you forgot to open it, please do so.\n";
                    output+="Otherwise delete it!";
                   // System.out.println(output);
                    WriteFile.write_general(output, "./src/results/OUTPUT",true);
                    errorFlag = true;
                }

                // Restore to the empty stacks (initial state of this if)
                if (flag == true) {
                    charStack.remove(charStack.peek());
                    positionStack.remove(positionStack.peek());
                    flag = false;
                }

            }

            i++;
        }

        // Break the method if there are errors
        if (errorFlag == true) {
            return errorFlag;
        }

        // Check if the stack is empty
        if (charStack.empty()) {
            //System.out.println("empty");
        } else {
            //System.out.println("NOT empty");
            for (i = 0; i < charStack.size(); i++) {
                //System.out.println(positionStack.elementAt(i));
                String output = "You forgot to close " + charStack.elementAt(i) + " at position " + positionStack.elementAt(i) + "\n";
                System.out.println(output);
                WriteFile.write_general(output, "./src/results/OUTPUT",true);
                return errorFlag = true;
            }

            charStack.removeAllElements();
            positionStack.removeAllElements();
        }

        return errorFlag;

    }
}
