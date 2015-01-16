package com.example.infotempo;

public class DayItem {
	private int day;
	private int month;
	private int year;
	private int couleur;
	public int getDay() {
		return day;
	}
	public int getMonth() {
		return month;
	}
	public int getYear() {
		return year;
	}
	public int getCouleur() {
		return couleur;
	}
	public void setCouleur(int cl) {
		couleur = cl;
	}
	public DayItem(int d, int m, int y, int cl) {
		day = d;
		month = m;
		year = y;
		couleur = cl;
	}
}
