package edu.unc.ils.mrc.hive.ir.tagging.dummy.ranking;

/*
 * Comparador.java
 * Copyright (C) 04-ene-2005 José Ramón Pérez Agüera y Rodrigo Sánchez Jiménez
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
 * Implementa un comparador en base al peso de un elemento de un vector. Util para reordenar por similitud.
 */

import java.util.Comparator;

public class ComparadorRanking implements Comparator<Rankeable>{
    
	public int compare(Rankeable obj1, Rankeable obj2) {
		Rankeable r1 = obj1;
		Rankeable r2 = obj2;
		return(r1.getRankingValue().compareTo(r2.getRankingValue())*-1);
	}
    
}

