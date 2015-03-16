package com.rameysoft.kewos.search.model;


public class Result {

	private String fileName;
	private String missingResidues;
	private String mutation;
	private String ec;
	private String expdta;
	private String chain;
	private String molecule;
	private String resolution;
	private String hetnam;
	
	public Result() {
    }
	
	public String getMutation() {
		return mutation;
	}
	public void setMutation(String mutation) {
		this.mutation = mutation;
	}
	public String getEc() {
		return ec;
	}
	public void setEc(String ec) {
		this.ec = ec;
	}
	public String getExpdta() {
		return expdta;
	}
	public void setExpdta(String expdta) {
		this.expdta = expdta;
	}
	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
		this.chain = chain;
	}
	public String getMolecule() {
		return molecule;
	}
	public void setMolecule(String molecule) {
		this.molecule = molecule;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String string) {
		this.resolution = string;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String b) {
		this.fileName = b;
	}
	public String getMissingResidues() {
		return missingResidues;
	}
	public void setMissingResidues(String missingResidues) {
		this.missingResidues = missingResidues;
	}

	public String getHetnam() {
		return hetnam;
	}

	public void setHetnam(String hetnam) {
		this.hetnam = hetnam;
	}
}
