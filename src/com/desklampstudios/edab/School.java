package com.desklampstudios.edab;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
class School {
	@Id Long id;
	@Index String name;
	@Index String short_name;
	String url;
	
	@Entity
	static class Team {
		@Parent School parent;
		@Id Long id;
		@Index String letter;
		String name;
		@Index int grade;
	}
	@Entity
	static class Semester {
		@Parent School parent;
		@Id String id;
		String abbr;
		@Index int startYear;
		@Index int semester;
	}
}