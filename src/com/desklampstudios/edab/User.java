package com.desklampstudios.edab;

import java.util.List;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotNull;

@Cache
@Entity
public class User {
	public class Courses { }

	enum Gender { male, female, other }
	enum AccountState {
		IMPORTED,      // user imported, has not logged in
		NEEDS_APPROVAL // user has tried to log in but not approved
	}


	public @Id              String id; // google userid
	public @Index           String fcps_id; // fcpsschools.net user email
	public @Index           String name;
	public                  String real_name;
	@Load(Courses.class)    List<Ref<Course>> courses;
	public                  Gender gender;
	private                 Text bio;
	@Index(IfNotNull.class) AccountState accountState;
	
	public String getBio() {
		if (bio == null) return null;
		else return bio.getValue();
	}
	public void setBio(String str) {
		if (str == null) bio = null;
		else bio = new Text(str);
	}
	
	@Override
	public String toString() {
		return "User object; name: " + name + ", id: " + id + ", fcps_id: " + fcps_id;
	}
}
