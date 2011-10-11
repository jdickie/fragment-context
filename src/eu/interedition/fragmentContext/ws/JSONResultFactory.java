package eu.interedition.fragmentContext.ws;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import eu.interedition.fragmentContext.text.TextContext;
import eu.interedition.fragmentContext.text.urifragident.TextFragmentIdentifier;

public class JSONResultFactory {

	public JSONObject createResult(
			JSONObject jsonArgs, TextContext context, TextFragmentIdentifier textFragmentIdentifier) 
					throws JSONException {
		JSONObject jsonResult = new JSONObject();
		jsonResult.put(ArgumentsParser.Field.uri.name(), 
				jsonArgs.getString(ArgumentsParser.Field.uri.name()).substring(
						0, jsonArgs.getString(ArgumentsParser.Field.uri.name()).indexOf("#")+1)
						+textFragmentIdentifier.toString());
		
		JSONObject jsonConstraint = new JSONObject();
		jsonConstraint.put(
				ArgumentsParser.Field.checksum.name(), 
				Hex.encodeHexString(context.getCheckSum()));

		jsonConstraint.put(
				ArgumentsParser.Field.position.name(), 
				textFragmentIdentifier.getTextScheme());
		
		JSONObject jsonContext = new JSONObject();
		jsonContext.put(
				ArgumentsParser.Field.before.name(), 
				context.getBeforeContext());
		jsonContext.put(
				ArgumentsParser.Field.after.name(), 
				context.getAfterContext());
		
		jsonConstraint.put(
				ArgumentsParser.Field.context.name(), 
				jsonContext.toString());
		
		jsonResult.put(
				ArgumentsParser.Field.constraint.name(), 
				jsonConstraint);
		
		return jsonResult;
	}
}
