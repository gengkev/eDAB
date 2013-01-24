package com.desklampstudios.edab;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
class School {
	@Id Long id;
	@Index String short_name;
	@Index String name;
	List<Team> teams;
	List<ExternalLink> external_links;
	
	@Embed
	class ExternalLink {
		String name;
		String url;
	}
	
	@Entity
	class Team {
		@Parent School parent;
		@Id Long id;
		@Index String letter;
		String name;
		int grade;
	}
}