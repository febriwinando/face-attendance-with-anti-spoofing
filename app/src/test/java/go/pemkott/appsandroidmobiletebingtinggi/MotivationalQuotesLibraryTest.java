package go.pemkott.appsandroidmobiletebingtinggi;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import go.pemkott.appsandroidmobiletebingtinggi.utils.MotivationalQuotesLibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class MotivationalQuotesLibraryTest {

    private static final List<String> FORBIDDEN_TERMS = Arrays.asList(
            "amsal", "korintus", "yakobus", "pengkhotbah", "roma", "efesus",
            "petrus", "galatia", "ibrani", "matius", "kolose", "lukas",
            "filipi", "tesalonika", "titus", "mazmur", "mikha", "markus",
            "yeremia", "yesaya", "alkitab", "kristus", "yesus", "roh kudus",
            "injil", "buah roh", "anak allah", "anak-anak allah", "bapa di sorga",
            "bapamu yang di sorga"
    );

    private static final Pattern STANDALONE_ROH_PATTERN = Pattern.compile("\\broh\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern VERSE_REF_PATTERN = Pattern.compile("\\b\\d+:\\d+\\b");

    @Test
    public void appendedQuotesPreserveOriginalAndAdd105WithAuthorAku() {
        List<MotivationalQuotesLibrary.Quote> quotes = MotivationalQuotesLibrary.getAllQuotes();
        assertEquals(496, quotes.size());
        assertEquals("Walt Disney", MotivationalQuotesLibrary.getQuoteById(1).getAuthor());
        assertEquals("Ir. Soekarno", MotivationalQuotesLibrary.getQuoteById(390).getAuthor());

        for (int i = 0; i < quotes.size(); i++) {
            MotivationalQuotesLibrary.Quote quote = quotes.get(i);
            assertEquals(i + 1, quote.getId());
            assertNotNull(quote.getQuote());
            assertFalse(quote.getQuote().trim().isEmpty());
            assertNotNull(quote.getAuthor());
            assertFalse(quote.getAuthor().trim().isEmpty());

            if (quote.getId() >= 391) {
                assertEquals("AKU", quote.getAuthor());
                String lower = quote.getQuote().toLowerCase(Locale.ROOT);
                for (String term : FORBIDDEN_TERMS) {
                    assertFalse("Unexpected term '" + term + "' in quote #" + quote.getId(), lower.contains(term));
                }
                assertFalse("Unexpected 'roh' in quote #" + quote.getId(),
                        STANDALONE_ROH_PATTERN.matcher(quote.getQuote()).find());
                assertFalse("Unexpected verse pattern in quote #" + quote.getId(),
                        VERSE_REF_PATTERN.matcher(quote.getQuote()).find());
            }
        }
    }

    @Test
    public void scheduledRandomAndLookupWorkAcross496Quotes() {
        assertNotNull(MotivationalQuotesLibrary.getScheduledQuote());
        assertNotNull(MotivationalQuotesLibrary.getTodayQuote());
        assertNotNull(MotivationalQuotesLibrary.getRandomQuote());
        assertEquals(496, MotivationalQuotesLibrary.getQuoteById(496).getId());
        assertEquals("AKU", MotivationalQuotesLibrary.getQuoteById(496).getAuthor());

        for (int i = 0; i < 20; i++) {
            MotivationalQuotesLibrary.Quote next = MotivationalQuotesLibrary.getRandomQuoteExcluding(1);
            assertNotNull(next);
            assertFalse(next.getId() == 1);
        }
    }
}
