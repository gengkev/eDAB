package com.desklampstudios.edab;

import java.util.List;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

@Entity
public class Course {
	@SuppressWarnings("unused")
	private Course() { }
	
	Course(Long id) {
		this.id = id;
	}
	
	private @Id Long id;
	public @Index String name;
	public String longName;
	private Text info;	
	// String fcps_id;
	public String bb_id;
	// String bb_long_id;
	@Load @Index List<Ref<User>> instructors;
	@Load List<Ref<User>> users;
	// @Load @Index Ref<Semester> semester;
	
	public Long getId() {
		return this.id;
	}
	public String getInfo() {
		return (info == null ? null : info.getValue());
	}
	public void setInfo(String str) {
		info = (str == null ? null : new Text(str));
	}
	
	@Override
	public String toString() {
		return "Course object; id=" + id + ", name=" + name;
	}
}
