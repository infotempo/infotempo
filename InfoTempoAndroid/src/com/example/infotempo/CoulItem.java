package com.example.infotempo;

public class CoulItem {
	public static final int BLEU = 1;
	public static final int BLANC = 2;
	public static final int ROUGE = 3;
	public static final int INCONNUE = -1;
	
	private String Titre;
	private String Desc;
	private int Couleur;
	private int nb1,nb2,nb3,tot1,tot2,tot3;
	private boolean isNbJours;
	private boolean isError;

	public String getTitre() {
		return Titre;
	}

	public void setTitre(String titre) {
		Titre = titre;
	}

	public String getTexte() {
		switch(Couleur) {
		case 0: return "";
		case BLEU: return "BLEU";
		case BLANC: return "BLANC";
		case ROUGE: return "ROUGE";
		default: return "indéterminée";
		}
	}

	public static int getColor(int cl) {
		switch(cl) {
		case 0: return InfoTempoApp.context.getResources().getColor(R.color.gris1);
		case BLEU: return InfoTempoApp.context.getResources().getColor(R.color.bleu);
		case BLANC: return InfoTempoApp.context.getResources().getColor(R.color.blanc);
		case ROUGE: return InfoTempoApp.context.getResources().getColor(R.color.rouge);
		default: return InfoTempoApp.context.getResources().getColor(R.color.gris2);
		}
	}

	public int getColor() {
		return getColor(Couleur);
	}
	
	public static int getTextColor(int cl) {
		switch(cl) {
		case BLANC: return 0xff000000;
		default: return 0xffffffff;
		}
	}

	public int getTextColor() {
		return getTextColor(Couleur);
	}
	
	public int getCouleur() {
		return Couleur;
	}

	public void setCouleur(int couleur) {
		Couleur = couleur;
		isNbJours = false;
	}

	public void setNbBleu(int nb, int tot) {
		nb1 = nb;
		tot1 = tot;
		isNbJours = true;
	}

	public void setNbBlanc(int nb, int tot) {
		nb2 = nb;
		tot2 = tot;
		isNbJours = true;
	}

	public void setNbRouge(int nb, int tot) {
		nb3 = nb;
		tot3 = tot;
		isNbJours = true;
	}

	public void addNb(int couleur) {
		switch(couleur) {
		case BLEU: {nb1++;break;}
		case BLANC: {nb2++;break;}
		case ROUGE: {nb3++;break;}
		}
	}
	
	public boolean isNbJours() {
		return isNbJours;
	}

	public String getNbJours(int cl) {
		switch(cl) {
		case BLEU: return Integer.toString(nb1)+"/"+Integer.toString(tot1);
		case BLANC: return Integer.toString(nb2)+"/"+Integer.toString(tot2);
		case ROUGE: return Integer.toString(nb3)+"/"+Integer.toString(tot3);
		default: return "";
		}
	}

	public String getNbJours2(int cl) {
		switch(cl) {
		case BLEU: return Integer.toString(nb1);
		case BLANC: return Integer.toString(nb2);
		case ROUGE: return Integer.toString(nb3);
		default: return "";
		}
	}
	
	public String getDesc() {
		return Desc;
	}

	public void setDesc(String desc) {
		Desc = desc;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}
}
