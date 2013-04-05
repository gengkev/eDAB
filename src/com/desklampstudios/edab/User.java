package com.desklampstudios.edab;

import java.util.List;

import com.desklampstudios.edab.School.Team;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

@Entity
class User {
	class LoadCourses { }
	
	enum Gender { MALE, FEMALE, OTHER }
	
	@Id String id; // google userid
	@Index public String name;
	public String real_name;
	Ref<Team> team;
	@Load(LoadCourses.class) @Index List<Ref<Course>> courses;
	@Index public String fcps_id; // fcpsschools.net user email
	Gender gender;
	
	@JsonGetter
	public String gender() {
		if (this.gender != null) {
			return this.gender.toString();
		} else {
			return null;
		}
	}
}
