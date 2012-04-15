package eu.interedition.fragmentContext.text;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.*;

import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;
import de.tud.kom.stringutils.tokenization.CharacterTokenizer;
import de.tud.kom.stringmatching.gst.*;
import eu.interedition.fragmentContext.Constraint;
import eu.interedition.fragmentContext.Context;
import eu.interedition.fragmentContext.Primary;

public class TextContext implements Context {

	public static final int DEFAULT_CONTEXTLENGTH = 20;

	/*
	 * Percentage of any given selection that should be stored. If selection is
	 * below DEFAULT_CONTEXTLENGTH, then the entire selection is stored.
	 * Otherwise, only the given percentage below of the beginning and end of
	 * the selection is used.
	 */
	private static double percentStorage = 0.15;

	public static enum HashType {
		MD5, SHA, Length
	};

	private byte[] checkSum;

	private HashType checkSumType;

	private int totalSelectionLength;

	String beforeContext;

	String afterContext;

	String beginSel;

	String endSel;

	String totalSelection;

	// Constructor::TextContext
	//
	// Generates a TextContext Object. 
	//
	// Generates before and after contexts based on given length and values
	// 
	// Parameters
	// 
	// * primary -- TextPrimary object to be tested against context
	// * constraint -- TextConstraint object attached to this context
	// * checkSumType -- Hash for checksum
	// * contextLength - length (in chars) of the context used for testing
	// 
	public TextContext(TextPrimary primary, TextConstraint constraint,
			HashType checkSumType, int contextLength) {
		super();
		this.checkSumType = checkSumType;
		//	Testing if content matches the bit-checksum tests
		
		this.checkSum = checkSum(primary.getContent(), checkSumType);

		int beforeStart = constraint.getStartPos() - contextLength;
		beforeStart = Math.max(0, beforeStart);
		int beforeEnd = constraint.getStartPos();

		int afterStart = constraint.getEndPos();
		int afterEnd = constraint.getEndPos() + contextLength;
		afterEnd = Math.min(primary.getContent().length(), afterEnd);

		// Evaluating how much of selected text to store
		this.totalSelectionLength = primary.getContent().length();
		int cLength = this.totalSelectionLength;
		if (this.totalSelectionLength > DEFAULT_CONTEXTLENGTH) {
			double half = (double) (this.totalSelectionLength / 2);
			cLength = (int) (Math.floor(half * percentStorage));
			this.beginSel = primary.getContent().substring(beforeEnd,
					(beforeEnd + cLength));
			this.endSel = primary.getContent().substring(
					(afterStart - cLength), afterStart);
			this.totalSelection = this.beginSel.concat(this.endSel);
		} else {
			// Use the entire selection
			this.beginSel = "";
			this.endSel = "";
			this.totalSelection = primary.getContent();
		}

		this.beforeContext = primary.getContent().substring(beforeStart,
				beforeEnd);
		this.afterContext = primary.getContent()
				.substring(afterStart, afterEnd);
	}

	/*
	 * Constructor::TextContext
	 * 
	 * Constructor no.2 for TextContext
	 * 
	 * TextConstraint constraint - Constraint to use as a base for testing
	 * 
	 * byte checkSum - Checksum to use to test the primary text
	 * 
	 * String beforeContext - String of characters expected before the annotation
	 * 
	 * String afterContext - String of characters expected after the annotation
	 * 
	 */
	public TextContext(TextConstraint constraint, byte[] checkSum,
			HashType checkSumType, String beforeContext, String afterContext) {
		super();
		this.checkSumType = checkSumType;
		this.checkSum = checkSum;

		this.beforeContext = beforeContext;
		this.afterContext = afterContext;
	}

	
	/*
	 * checkSum 
	 * 
	 * Checks to make sure return value of content is is UTF-8,
	 * and has recognized mimetype text/plain
	 * 
	 * Parameters
	 * content - String to test 
	 * 
	 * checksumType - byte sequence to use to test against
	 * 
	 */
	static byte[] checkSum(String content, HashType checksumType) {
		byte[] contentBytes;
		try {
			contentBytes = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("Encoding unknown!");
		}
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(checksumType.name());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("Algorithm unknown!");
		}
		byte[] digest = md.digest(contentBytes);

		return digest;
	}

	@Override
	public boolean verify(Primary primary) {
		if (!(primary instanceof TextPrimary))
			throw new IllegalArgumentException();
		TextPrimary textPrimary = (TextPrimary) primary;

		byte[] digest = checkSum(textPrimary.getContent(), this.checkSumType);

		return Arrays.equals(digest, this.checkSum);

	}

	/*
	 * createGST and gstMATCH
	 * 
	 * Methods to implement the GST-TILING methodology of matching strings.
	 */
	private GST createGST(int needleLength, String haystack) {
		GST gst = new GST(haystack);
		// Needs to be at least a third of the needle length to
		// count as a match
		gst.setMinimumTileLength((int) Math.ceil(needleLength / 3));

		return gst;
	}

	// ## TextConstraint gstMatch
	//
	// Matches a constraint within passed content. Makes sure that
	// the constraint text is found within the content, but in order to
	// be a true match, it has to match in or near the same location as
	// the original constraint
	//
	// Parameters:
	//
	// * primaryContent - String - content to find context within
	// 
	// * originalConstraint - TextConstraint - object to test internal constraint against
	private TextConstraint gstMatch(String primaryContent,
			TextConstraint originalConstraint) throws NoMatchFoundException {
		GST g = createGST(this.beforeContext.length(), primaryContent);

		g.match(this.beforeContext);
		if (g.getTiles().size() == 0)
			throw new Context.NoMatchFoundException();
		int startMatch = -1;
		int maxMatchIndex = originalConstraint.getStartPos()
				- (this.beforeContext.length() + (int) Math
						.ceil((this.beforeContext.length() / 3)));
		int minMatchIndex = originalConstraint.getStartPos()
				- (int) Math.ceil(Math.ceil(this.beforeContext.length() / 3));
		int i;
		// go through each matched TILE and see
		// if the match is close to our context
		for (GSTTile item : g.getTiles()) {
			i = item.getStart();
			if (i > startMatch && i > minMatchIndex && i <= maxMatchIndex) {
				startMatch = item.getStart();
			}
		}
		// Check if no matches found
		if (startMatch < 1)
			throw new Context.NoMatchFoundException();

		minMatchIndex = originalConstraint.getEndPos()
				+ (this.afterContext.length() + (int) Math
						.ceil((this.beforeContext.length() / 3)));
		maxMatchIndex = originalConstraint.getEndPos()
				+ this.afterContext.length();
		int endMatch = -1;

		for (GSTTile item : g.getTiles()) {
			i = item.getStart() + item.getLength();
			if (i > endMatch && i > minMatchIndex && i <= maxMatchIndex) {
				endMatch = item.getStart();
			}
		}

		if (endMatch < 1)
			throw new Context.NoMatchFoundException();

		return new TextConstraint(startMatch, endMatch);
	}

	private ShingleCloud createSC(int needleLength, String hayStack) {

		ShingleCloud sc = new ShingleCloud(hayStack);
		sc.setTokenizer(new CharacterTokenizer());

		int nGramSize = Math.min((int) (needleLength), 20);
		sc.setNGramSize(nGramSize);

		sc.setMinimumNumberOfOnesInMatch((int) (1));
		sc.setSortMatchesByRating(true);

		return sc;
	}

	private TextConstraint shingleCloudMatch(String primaryContent)
			throws NoMatchFoundException {
		// Using the TILING method
		ShingleCloud sc = createSC(this.beforeContext.length(), primaryContent);

		// find the text before the annotation
		sc.match(this.beforeContext);
		if (sc.getMatches().isEmpty())
			throw new Context.NoMatchFoundException();

		int startPos = sc.getMatches().get(0).getStart()
				+ sc.getMatches().get(0).getLength();

		// find text after annotation
		sc = createSC(this.afterContext.length(), primaryContent);

		sc.match(this.afterContext);
		if (sc.getMatches().isEmpty())
			throw new Context.NoMatchFoundException();

		int endPos = sc.getMatches().get(0).getStart();

		return new TextConstraint(startPos, endPos);
	}

	private TextConstraint exactMatch(String primaryContent,
			TextConstraint originalConstraint) throws NoMatchFoundException {

		// find the text before the annotation
		// int startPos = primaryContent.indexOf(this.beforeContext);
		int startPos = findClosestIndexOf(this.beforeContext,
				originalConstraint.getStartPos() - this.beforeContext.length(),
				primaryContent);

		startPos += this.beforeContext.length();

		// find text after annotation
		// int endPos = primaryContent.indexOf(this.afterContext);
		int endPos = findClosestIndexOf(this.afterContext,
				originalConstraint.getEndPos(), this.totalSelection);

		if (endPos < 0 || startPos < 0) {
			// search through the selected content

			int positionTotal = 0;
			if (this.beginSel.length() > 0 && this.endSel.length() > 0) {
				int originalPosTotal = (originalConstraint.getStartPos() + (originalConstraint
						.getEndPos() - this.endSel.length()));

				// search beginning source selection, then end
				// selection, respectively
				int beginTotal = findClosestIndexOf(this.beginSel,
						originalConstraint.getStartPos(), primaryContent);

				int afterTotal = findClosestIndexOf(
						this.endSel,
						(originalConstraint.getEndPos() - this.endSel.length()),
						primaryContent);

				positionTotal = beginTotal + afterTotal;

				if (Math.abs(positionTotal - originalPosTotal) > 5) {
					return null;
				}

			} else {
				// search through total selection
				positionTotal = findClosestIndexOf(this.totalSelection,
						originalConstraint.getStartPos(), primaryContent);
				if (positionTotal < 0)
					return null;

			}

		}

		return new TextConstraint(startPos, endPos);
	}

	private int findClosestIndexOf(String context, int oldIndex, String content) {

		Matcher matcher = Pattern.compile(Pattern.quote(context)).matcher(
				content);
		int index = 0;

		while (matcher.find()) {
			if (Math.abs(oldIndex - matcher.start()) < (Math.abs(oldIndex
					- index))) {
				index = matcher.start();
			}
		}

		return index;
	}

	@Override
	public Constraint match(Primary primary, TextConstraint originalConstraint)
			throws Context.NoMatchFoundException {
		if (!(primary instanceof TextPrimary))
			throw new IllegalArgumentException();
		TextPrimary textPrimary = (TextPrimary) primary;

		// TextConstraint result = shingleCloudMatch(textPrimary.getContent());
		TextConstraint result = exactMatch(textPrimary.getContent(),
				originalConstraint);
		if (result == null) {
			// perform fuzzy matching
			result = gstMatch(textPrimary.getContent(), originalConstraint);
		}

		// sanity check
		if (result.getEndPos() < result.getStartPos())
			throw new Context.NoMatchFoundException();

		return result;
	}

	public byte[] getCheckSum() {
		return checkSum;
	}

	public String getBeforeContext() {
		return beforeContext;
	}

	public String getAfterContext() {
		return afterContext;
	}
}
