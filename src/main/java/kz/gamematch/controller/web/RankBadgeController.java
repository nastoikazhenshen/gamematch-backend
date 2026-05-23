package kz.gamematch.controller.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RankBadgeController {

    private static final Map<String, RankStyle> GAME_STYLES = Map.of(
            "dota-2", new RankStyle("#4c1515", "#d8a13f", "#f6d98b"),
            "cs2", new RankStyle("#1f2b39", "#d29b34", "#e9c46a"),
            "valorant", new RankStyle("#2b2f3a", "#ff4655", "#ff8a92"),
            "pubg", new RankStyle("#1f3526", "#d0a448", "#ead08a")
    );

    private static final Map<String, String> RANK_LABELS = Map.ofEntries(
            Map.entry("herald", "Herald"),
            Map.entry("guardian", "Guardian"),
            Map.entry("crusader", "Crusader"),
            Map.entry("archon", "Archon"),
            Map.entry("legend", "Legend"),
            Map.entry("ancient", "Ancient"),
            Map.entry("divine", "Divine"),
            Map.entry("immortal", "Immortal"),
            Map.entry("silver-i", "Silver I"),
            Map.entry("silver-ii", "Silver II"),
            Map.entry("silver-iii", "Silver III"),
            Map.entry("silver-iv", "Silver IV"),
            Map.entry("silver-elite", "Silver Elite"),
            Map.entry("silver-elite-master", "Silver Elite Master"),
            Map.entry("gold-nova-i", "Gold Nova I"),
            Map.entry("gold-nova-ii", "Gold Nova II"),
            Map.entry("gold-nova-iii", "Gold Nova III"),
            Map.entry("gold-nova-master", "Gold Nova Master"),
            Map.entry("master-guardian-i", "Master Guardian I"),
            Map.entry("master-guardian-ii", "Master Guardian II"),
            Map.entry("master-guardian-elite", "Master Guardian Elite"),
            Map.entry("distinguished-master-guardian", "Distinguished Master Guardian"),
            Map.entry("legendary-eagle", "Legendary Eagle"),
            Map.entry("legendary-eagle-master", "Legendary Eagle Master"),
            Map.entry("supreme-master-first-class", "Supreme Master First Class"),
            Map.entry("global-elite", "Global Elite"),
            Map.entry("iron", "Iron"),
            Map.entry("bronze", "Bronze"),
            Map.entry("silver", "Silver"),
            Map.entry("gold", "Gold"),
            Map.entry("platinum", "Platinum"),
            Map.entry("diamond", "Diamond"),
            Map.entry("ascendant", "Ascendant"),
            Map.entry("radiant", "Radiant"),
            Map.entry("master", "Master"),
            Map.entry("grandmaster", "Grandmaster"),
            Map.entry("conqueror", "Conqueror")
    );

    @GetMapping(value = "/rank-badges/{game}/{rank}.svg", produces = "image/svg+xml")
    public ResponseEntity<String> rankBadge(@PathVariable String game, @PathVariable String rank) {
        RankStyle style = GAME_STYLES.getOrDefault(game, new RankStyle("#243447", "#7aa2f7", "#b7c8ff"));
        String label = RANK_LABELS.getOrDefault(rank, toTitle(rank));
        String initials = initials(label);
        int level = Math.max(1, Math.abs(rank.hashCode()) % 8 + 1);

        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 96 96" role="img" aria-label="%s rank">
                  <defs>
                    <linearGradient id="g" x1="0" x2="1" y1="0" y2="1">
                      <stop offset="0" stop-color="%s"/>
                      <stop offset="1" stop-color="%s"/>
                    </linearGradient>
                    <filter id="s" x="-20%%" y="-20%%" width="140%%" height="140%%">
                      <feDropShadow dx="0" dy="5" stdDeviation="4" flood-color="#000" flood-opacity=".32"/>
                    </filter>
                  </defs>
                  <rect width="96" height="96" rx="18" fill="%s"/>
                  <path d="M48 10l30 12v24c0 19-12 32-30 40C30 78 18 65 18 46V22l30-12z" fill="url(#g)" filter="url(#s)"/>
                  <path d="M48 18l21 9v18c0 13-8 23-21 29-13-6-21-16-21-29V27l21-9z" fill="none" stroke="%s" stroke-width="3" opacity=".75"/>
                  <text x="48" y="53" text-anchor="middle" font-family="Arial, sans-serif" font-size="24" font-weight="800" fill="#fff">%s</text>
                  <text x="48" y="70" text-anchor="middle" font-family="Arial, sans-serif" font-size="10" font-weight="700" fill="#fff" opacity=".86">LVL %d</text>
                </svg>
                """.formatted(escape(label), style.mid(), style.light(), style.dark(), style.light(), escape(initials), level);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(svg);
    }

    private String initials(String label) {
        String[] parts = label.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String toTitle(String slug) {
        String[] parts = slug.replace('-', ' ').split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
        }
        return result.toString();
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record RankStyle(String dark, String mid, String light) {
    }
}
