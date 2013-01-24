package com.desklampstudios.edab;

import java.util.List;

import com.desklampstudios.edab.School.Team;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
class User {
	@Id Long id;
	String given_name;
	String family_name;
	@Index String name;
	@Index Key<Team> team;
	@Index List<Key<Course>> courses;
	@Index String fcps_id;
	String gplus_id;
	String fb_id;
	int gender; // 0 - male, 1 - female, 2 - other
	
	String getGender() {
		switch (this.gender) {
		case 0:
			return "male";
		case 1:
			return "female";
		case 2:
			return "other";
		}
		return "";
	}
}