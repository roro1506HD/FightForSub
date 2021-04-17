import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This file is a part of FightForSub project.
 *
 * @author roro1506_HD
 */
public class Score {

    public static void main(String[] args) throws Exception {
        File file = new File("E:/Desktop/Projet avengers/FightForSub/logs.log");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        List<String> malus = Arrays.asList("TheGuill84",
                "Guep",
                "LoupioSixSix",
                "nemenems",
                "BYSLIDE",
                "Etoiles",
                "Fukano",
                "TheGuill84",
                "Guep",
                "LoupioSixSix",
                "GrxyLight",
                "TwitchGlast",
                "Daiko",
                "Libe_",
                "TheVicMC",
                "MC_Ika");

        int place = 94;
        int prefix = "[20:52:51] [Client thread/INFO]: [CHAT] ".length();
        Map<String, Integer> kills = new HashMap<>();
        Map<String, Integer> placement = new HashMap<>();
        Map<String, Integer> deaths = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("a été tué par")) {
                String[] names = line.replace(" a été tué par", "").substring(prefix).split(" ");
                String death = names[0];
                String killer = names[1];
                Integer oldKills = kills.get(killer);
                Integer oldDeaths = deaths.get(death);

                kills.put(killer, oldKills == null ? 1 : oldKills + 1);
                deaths.put(death, oldDeaths == null ? 1 : oldDeaths + 1);

                if (!deaths.containsKey(killer))
                    deaths.put(killer, 0);

                if (oldDeaths != null && oldDeaths + 1 == (malus.contains(death) ? 2 : 3))
                    placement.put(death, place--);
            } else if (line.contains("est mort")) {
                String death = line.replace(" est mort.", "").substring(prefix);
                Integer oldDeaths = deaths.get(death);

                deaths.put(death, oldDeaths == null ? 1 : oldDeaths + 1);

                if (oldDeaths != null && oldDeaths + 1 == (malus.contains(death) ? 2 : 3))
                    placement.put(death, place--);
            }
        }

        for (String name : deaths.keySet()) {
            if (!placement.containsKey(name))
                placement.put(name, place--);

            if (!kills.containsKey(name))
                kills.put(name, 0);
        }

        PrintWriter writer = new PrintWriter(new File("round-2.csv"));

        for (String name : placement.keySet())
            writer.println(name + ";" + placement.get(name) + ";" + kills.get(name));

        writer.flush();
        writer.close();

        System.out.println("Kills: " + kills.size());
        System.out.println("Deaths: " + deaths.size());
        System.out.println("Placement: " + placement.size());
    }
}
