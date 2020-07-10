package org.grammaticalframework.grammarlex.ViewModel;

import org.daison.*;

public class SenseSchema {
	public static enum Status {
		Guessed, Unchecked, Changed, Checked
	}

	@DaisonDataFields(fields={"language","status"})
	public static class LanguageStatus {
		public String language;
		public Status status;

		public String toString() {
			return language+": "+status;
		}
	}

	@DaisonDataFields(fields={"page_url","img_url"})
	public static class ImageInfo {
		public String page_url;
		public String img_url;
	}

	@DaisonDataFields(fields={"lex_fun","status","synset_id",
                              "domains","images","example_ids",
                              "frame_ids"})
	public static class Lexeme {
		public String lex_fun;
		public LanguageStatus[] status;

		@DaisonMaybeField()
		public Long synset_id;

		public String[] domains;
		public ImageInfo[] images;
		public long[] example_ids;
		public long[] frame_ids;
	}

	@DaisonDataFields(fields={"start","end"})
	public static class Interval {
		public long start;
		public long end;
	}

	@DaisonDataFields(fields={"synsetOffset","parents",
	                          "children","gloss"})
	public static class Synset {
		public String synsetOffset;
		public long[] parents;
		public Interval[] children;
		public String gloss;
	}

	public static final Table<Lexeme> lexemes;
	public static final Index<Lexeme,String> lexemes_fun;

	public static final Table<Synset> synsets;

	static {
		lexemes = new Table<Lexeme>("lexemes", Lexeme.class);
		lexemes_fun = new Index<Lexeme,String>(lexemes, "fun", String.class, (lex) -> lex.lex_fun);
		lexemes.addIndex(lexemes_fun);

		synsets = new Table<Synset>("synsets", Synset.class);
	}
}
