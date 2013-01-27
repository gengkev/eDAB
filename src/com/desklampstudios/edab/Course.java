package com.desklampstudios.edab;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
class Course {
	@Id Long id;
	@Index String name;
	String longName;
	String fcps_id;
	String bb_id;
	String bb_long_id;
	@Index List<Key<User>> instructors;
	@Index Key<Semester> semester;
	
	@Entity
	class Semester {
		@Id String id;
	}
}
