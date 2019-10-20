package misterl2.sfutilities.util;

import java.util.Date;

public class TimeConverter {

    public static String secondsToTimeString(long timeInDeciSeconds) { //The time is actually in 0.1s to provide compromise between accuracy and storage space
        String relativeTime;
        long timeInSeconds = timeInDeciSeconds / 10; //Convert to actual seconds
        long days = Math.floorDiv(timeInSeconds, (3600 * 24));
        timeInSeconds -= (days * 3600 * 24);

        long hours = Math.floorDiv(timeInSeconds,3600);
        timeInSeconds -= (hours * 3600);

        if(days!=0) {
            relativeTime = days + " days ";
            if(days<7) { //If days is >7, hours are irrelevant, so only include when days<7
                relativeTime += "and " + hours + " h";
            }
            return relativeTime;
        }


        long minutes = Math.floorDiv(timeInSeconds,60);
        timeInSeconds -= (minutes*60);

        if(hours!=0) {
            relativeTime = hours + " h ";
            if(hours<5) {
                relativeTime += "and " + minutes + " min";
            }
            return relativeTime;
        }

        relativeTime = minutes + " min ";
        if(minutes<30) {
            relativeTime += "and " + timeInSeconds + " sec"; //timeInSeconds is now the remaining time in seconds
        }

        return relativeTime;
    }

    public static long getUnixTimeSinceRelease() {
        return (new Date().getTime() - 1571427735000L)/ 100L; //in 0.1s. To save storage space in the DB, it subtracts the unixtime till 18.10.2019 (time of first release)
    }
}
