package com.desklampstudios.edab;

import java.util.List;

import com.desklampstudios.edab.School.Team;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

@Entity
class User {
	enum Gender { MALE, FEMALE, OTHER }
	
	@Id String id; // same as fcps_id
	@Index String name;
	String real_name;
	@Load @Index Ref<Team> team;
	@Load @Index List<Ref<Course>> courses;
	@Index String fcps_id;
	Gender gender;
	
	String getGender() {
		if (this.gender != null) {
			return this.gender.toString();
		} else {
			return null;
		}
	}
}