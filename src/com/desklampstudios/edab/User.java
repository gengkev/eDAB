package com.desklampstudios.edab;

import java.util.List;

import com.desklampstudios.edab.School.Team;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
class User {
	@Id String id; // same as fcps_id
	@Index String name;
	String real_name;
	@Index Key<Team> team;
	@Index List<Key<Course>> courses;
	@Index String fcps_id;
	Gender gender;
	
	enum Gender {
		MALE, FEMALE, OTHER
	}
}