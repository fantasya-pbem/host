package de.x8bit.Fantasya.util.lang;

import de.x8bit.Fantasya.util.Random;

/** Erzeugt zufaellige Silbenfolgen aus einem gegebenen Pool.
 *
 * Nuetzlich, um Zufallsnamen zu vergeben. Das ist auch fast der einzige Zweck.
 *
 * @author hapebe
 */
public class NonsenseTexter {

	/** Erzeugt ein Zufallswort mit gegebener Silbenzahl. */
	public static String makeNonsenseWort(int silbenZahl) {

        StringBuilder sb = new StringBuilder();
        for (int i=0; i < silbenZahl; i++) {
            sb.append(silben[Random.rnd(0, silben.length)]);
        }

        return sb.toString();
    }

	private static final String[] silben = new String[] {
        "ab", "ach", "al", "all", "am", "an", "an", "an", "an", "ang", "ank", "ant", "am", "art", "asp", "ast", "au", "auf",
        "ba", "bar", "batz", "be", "be", "be", "be", "bei", "bei", "ben", "ben", "ber", "betz", "bung", "bra", "brach", "bring", "bral", "brat", "brie",
        "bast", "bruch", "blun", "brenn", "brech", "berg", "belm", "blum", "ber",
        "da", "da", "dach", "dan", "dank", "de", "de", "de", "dem", "den", "den", "den", "den", "der", "dich", "ding", "dist", "disch", "dig", "do", "dung",
        "dra", "dreh", "drotz", "drum", "dupf", "dutz", "dreck", "druck", "drast", "drell",
        "ech", "ei", "ein", "ein", "ein", "ein", "ein", "eil", "en", "end", "end", "end", "er", "er", "er", "erst", "es", "ex",
        "fa", "fach", "fan", "fan", "fand", "fang", "fen", "fein", "fesch", "fi", "fin", "fin", "fitz", "fing", "fisch", "fetz", "fro", "flun", "fung", "fland", "flank", "flink", "fron", "fram", "fran", "frang",
        "gar", "ge", "ge", "ge", "ge", "gelb", "gell", "gei", "gen", "gen", "ger", "ger", "gin", "ging", "gol", "gung", "gra", "glei", "grein", "grad", "grund",
        "hab", "hal", "hal", "halb", "half", "han", "hang", "har", "har", "hau", "he", "hei", "heil", "held", "helf", "her", "her", "her", "her", "hin", "him", "hold", "holz", "hun",
        "ich", "in", "in", "in", "im", "itz",
        "ja", "je", "je", "jan", "jen", "jasch", "jetz",
        "ka", "kal", "kan", "kar", "ken", "ken", "ken", "kenn", "kei", "ker", "kepf", "kin", "kill", "kost", "kor", "kun", "kla", "klan", "klar", "klei", "klein", "kra", "kron", "kri",
        "la", "la", "la", "lab", "lad", "lach", "lan", "lanf", "lang", "lang", "lap", "lapf", "lar", "lat", "latz",
        "le", "lech", "lei", "leib", "lein", "leit", "len", "len", "len", "len", "len", "ler", "lett", "letz", "leck",
        "lich", "lich", "lig", "ling", "lieb", "lied", "lief", "lieg", "lieh", "lien", "liem", "lies", "liet", "litz",
        "lob", "loch", "log", "lohn", "lor", "lot", "lopf", "lost", "lotz",
        "lud", "luch", "lug", "lun", "lust", "lupf", "lutz",
        "ma", "ma", "ma", "ma", "mach", "mag", "mai", "mal", "malf", "man", "mar", "man", "mar", "mark", "mast", "mat", "malt", "matz", "masch",
        "me", "me", "mech", "mehl", "mehr", "men", "men", "men", "men", "mend", "mer", "mert", "merst", "mess", "mest", "melt", "metz", "mei", "meid", "mein", "meik", "meil",
        "mich", "min", "mild", "miel", "mist", "mig", "ming", "misch",
        "moch", "mog", "mol", "mon", "morf", "mord", "morst", "motz",
        "muff", "mug", "muld", "mulm", "murch", "musch",
        "na", "nach", "nag", "nah", "nann", "narn", "nahl", "nau", "naum", "nast", "natz", "nart", "nasch", "nang", "napf",
        "ne", "neff", "neft", "nehm", "neng", "nen", "nen", "nen", "nen", "nem", "nest", "nesch", "nepf", "nert", "nein",
        "nig", "nicht", "nimm", "nist",
        "noch", "nord", "nost", "nold", "nonk", "norg", "nohm", "noff", "nopf",
        "nug", "null", "nun", "nur", "nuss", "nust", "nutz",
        "ob", "och", "oft", "ohn", "ohl", "ohr", "onk", "orb", "ord", "orf", "org", "ork", "orst", "ort", "ost", "otz",
        "pach", "palm", "past", "patz", "pech", "per", "ping", "potz", "putz", "pust",
        "pfad", "pfang", "pfaff", "pfann", "pfahl", "pfar", "pfeff", "pfeif", "pferd", "pfett", "pfich", "pfing", "pfitz", "pfort", "pfolz", "pfund",
        "qual", "quer", "quat", "quint", "quo", "queng", "quer",
        "rad", "rag", "rach", "rack", "ran", "rahl", "rahm", "rar", "rat", "rast", "rasch", "ranz",
        "re", "rech", "reid", "reif", "reig", "reim", "reg", "reh", "relf", "ren", "ren", "ren", "renk", "renz", "repp", "rer", "res", "rest", "rest", "ret", "rett", "retz",
        "ried", "rich", "rick", "rig", "ring", "rind", "rink", "rimp", "ripp", "rist", "ritz",
        "roch", "rod", "roff", "roh", "roll", "ron", "ron", "rost", "rot", "rott", "rotz",
        "ruch", "ruck", "rund", "ruf", "rupf", "ruh",
        "sa", "sach", "sad", "saff", "sag", "sah", "saal", "salb", "san", "sand", "sang", "sapf", "sarg", "sarz", "sass", "satt", "satz", "sau", "sauf", "saug", "saum",
        "schad", "schaft", "schand", "schlang", "schlaf", "schlupf", "schon", "schutt", "schutz", "schen", "schen", "schen", "schild", "schind", "schirr", "schilf", "schig", "schick",
        "se", "se", "se", "seft", "seh", "sei", "sei", "selb", "sell", "selt", "sen", "sen", "sen", "senn", "ser", "serm", "sett", "setz",
        "sig", "sich", "sich", "sich", "sift", "sieg", "sicht", "sinn", "sing", "sing", "sing", "sitz",
        "so", "sod", "sog", "sock", "soll", "sold", "somm", "sonst", "sort", "soss", "sott",
        "spa", "spang", "spalt", "spat",
        "spech", "spelz", "speck", "speer", "sperr",
        "spiel", "spitz", "spind",
        "spor", "spur",
        "splen", "splein", "spliss",
        "sprach", "sproch", "spruch", "sprech", "sprenz",
        "stach", "stand", "stapf", "sten", "sten", "stech", "steig", "steil", "stein", "stiel", "stopf", "stur", "stund",
        "strand", "strack", "streck", "strich", "strumm",
        "such", "sund", "summ", "sulz",
        "ta", "tal", "tag", "tarn", "tau",
        "te", "tech", "tee", "teg", "teil", "ten", "ten", "ten", "ter", "ter", "ter", "tes", "tesch", "tet", "tet", "teu",
        "tin", "tim", "ting", "tich", "tig", "tier", "tieg",
        "tob", "tod", "toch", "told", "tong",
        "tung", "tung", "tupf", "turm",
        "tra", "trau", "traum", "trat", "treib", "tre", "treu", "trenn", "trepp", "tret", "tritt", "trimm", "tri", "trol", "trog", "trund",
        "uch", "und", "ul", "um", "un", "un", "un", "un", "un", "utz", "upf", "usch", "ur", "urg",
        "va", "ver", "ver", "ver", "veh", "veil", "viel", "vind", "vint", "voll", "von",
        "wa", "war", "wal", "wam", "was", "we", "weg", "weng", "weck", "weh", "wei", "wein", "weil", "well", "welch", "welt",
        "wem", "wen", "wen", "wenn", "wetz", "wett", "wert", "win", "wie", "wild", "wisch", "witz", "wog", "wo", "wung", "wund",
        "xa", "xen",
        "yen",
        "za", "zach", "zahm", "zahl", "zapf", "zehn", "zett", "zech", "zisch", "zick", "zong", "zur", "zu",
    };



}
