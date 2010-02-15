package edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Ranking.java
 * Copyright (C) 08-ene-2005 José Ramón Pérez Agüera y Rodrigo Sánchez Jiménez
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/**
 * @author jose
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Ranking {
	
	private ArrayList<Rankeable> ranking;
	
	public Ranking(){
		this.ranking= new ArrayList<Rankeable>();
	}
	
	public void addValor(Rankeable r){
		this.ranking.add(r);
	}
	
	public Rankeable getValor(int i){
		return this.ranking.get(i);
	}
	
	public int size()
	{
		return this.size();
	}
	
	public ArrayList<Rankeable> getRanking(){
		Collections.sort(this.ranking, new ComparadorRanking());
		return this.ranking;
	}
	
	public List<Rankeable> getTop(int n)
	{
		return this.ranking.subList(0, n);
	}

}
