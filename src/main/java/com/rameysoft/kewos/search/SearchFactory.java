package com.rameysoft.kewos.search;

import java.nio.CharBuffer;
import java.util.EnumMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rameysoft.kewos.search.model.Result;

public class SearchFactory {
	
	private EnumMap<SearchType,SearcherI> table = null;
	
	private static volatile SearchFactory $this = null;
	
	public static SearchFactory getInstance(){
		if($this == null){
			synchronized (SearchFactory.class) {
				$this = new SearchFactory();
				$this.initPredefined();
			}			
		}		
		return $this;
	}
	
	private SearchFactory(){
		table = new EnumMap<SearchType, SearcherI>(SearchType.class);
	}

	private void initPredefined() {
		table.clear();
		table.put(SearchType.CHAIN, $this.new Chain());
		table.put(SearchType.EC, $this.new Ec());
		table.put(SearchType.EXPDTA, $this.new ExpDta());
		table.put(SearchType.HETNAM, $this.new Hetnam());
		table.put(SearchType.MISSING_RESIDUES, $this.new MissingResidues());
		table.put(SearchType.MOLECULE, $this.new Molecule());
		table.put(SearchType.MUTATION, $this.new Mutation());
		table.put(SearchType.RESOLUTION, $this.new Resolution());
	}
	
	
	public static enum SearchType {
		MISSING_RESIDUES,MUTATION,RESOLUTION,EC,EXPDTA,CHAIN,MOLECULE,HETNAM;
	}

	public interface SearcherI {

		final static Pattern MISSING_RESIDUES = Pattern.compile("(MISSING RESIDUES)");
		final static Pattern MUTATION = Pattern.compile("(MUTATION)");
		final static Pattern RESOLUTION = Pattern.compile("(RESOLUTION).*");
		final static Pattern EC = Pattern.compile("( EC).*");
		final static Pattern EXPDTA = Pattern.compile("(EXPDTA).*");
		final static Pattern CHAIN = Pattern.compile("(CHAIN).*");
		final static Pattern MOLECULE = Pattern.compile("(MOLECULE).*");
		final static Pattern HETNAM = Pattern.compile("^(HETNAM).*");
		
		public Result doSearch(Result result, CharBuffer buf );
	}
	
	private class MissingResidues implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = MISSING_RESIDUES.matcher(buf);
			result.setMissingResidues(matcher.find()+"");
			return result;
		}
		
	}
	
	private class Mutation implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = MUTATION.matcher(buf);
			result.setMutation(matcher.find()+"");
			return result;
		}
		
	}
	private class Resolution implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = RESOLUTION.matcher(buf);
				String val = "NMR";
				while(matcher.find()) {
					String text = matcher.group();
					Scanner st = new Scanner(text);
			        try{
			        	while (!st.hasNextDouble()){
			        		st.next();
			        	}
			        	val = st.nextDouble()+"";
			        	break;
			        }catch(NoSuchElementException e){}
			        st.close();
				}
				result.setResolution(val);
			return result;
		}
		
	}
	
	private class Ec implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = EC.matcher(buf);
			if(matcher.find()) {
				String g = matcher.group().trim();
				for(String s: g.split(":|;")){
					if(!s.equals("EC") && !s.isEmpty()){
						result.setEc(s.trim());
					}
				}
						        
			} else {
				result.setEc("");
			}
			return result;
		}
		
	}
	
	private class ExpDta implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = EXPDTA.matcher(buf);
				if(matcher.find()) {

 					String g = matcher.group().trim();
 					for(String s: g.split("  ")){
 						if(!s.equals("EXPDTA") && !s.isEmpty()){	
 							result.setExpdta(s.trim());
 						}
 					}
 							        
 				} else {
 					result.setExpdta("");
 				}
			return result;
		}
		
	}
	
	private class Chain implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = CHAIN.matcher(buf);
				if(matcher.find()) {
 					String g = matcher.group().trim();
 					for(String s: g.split(":|;")){
 						if(!s.equals("CHAIN") && !s.isEmpty()){
 							result.setChain(s.trim());
 						}
 					}
 							        
 				} else {
 					result.setChain("");
 				}
			return result;
		}
		
	}
	
	private class Molecule implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = MOLECULE.matcher(buf);
				if(matcher.find()) {
 					String g = matcher.group().trim();
 					for(String s: g.split(":|;")){
 						if(!s.equals("MOLECULE") && !s.isEmpty()){	
 							result.setMolecule(s.trim());
 						}
 					}
 							        
 				} else {
 					result.setMolecule("");
 				}
			return result;
		}
		
	}
	
	private class Hetnam implements SearcherI{

		@Override
		public Result doSearch(Result result,  CharBuffer buf) {
			Matcher matcher = HETNAM.matcher(buf);
			result.setHetnam(matcher.find()+"");
			return result;
		}
		
	}
	
	public SearcherI removeSearchAble(SearchType type) {
		return table.remove(type);
	}
	
	public SearcherI getSearchAble(SearchType type) {
		return table.get(type);
	}
	
	public EnumMap<SearchType,SearcherI> getSearchAbles() {
		return table;
	}
	
	public  void putSearchAble(SearchType type) {
		if(table.containsKey(type)){
			return;
		}
		switch(type){
		case CHAIN:
			table.put(type, getInstance().new Chain());
			break;
		case EC:
			table.put(type, getInstance().new Ec());
			break;
		case EXPDTA:
			table.put(type, getInstance().new ExpDta());
			break;
		case HETNAM:
			table.put(type, getInstance().new Hetnam());
			break;
		case MISSING_RESIDUES:
			table.put(type, getInstance().new MissingResidues());
			break;
		case MOLECULE:
			table.put(type, getInstance().new Molecule());
			break;
		case MUTATION:
			table.put(type, getInstance().new Mutation());
			break;
		case RESOLUTION:
			table.put(type, getInstance().new Resolution());
			break;
		default:
			throw new IllegalArgumentException(type + "is non existing Searchable");
		
		}
		
	}
	
	public  void clearAllSearchAbles() {
		table.clear();
	}
}
