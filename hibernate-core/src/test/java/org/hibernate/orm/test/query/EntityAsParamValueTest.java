/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.query;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DomainModel(
		annotatedClasses = {
				EntityAsParamValueTest.Event.class,
				EntityAsParamValueTest.Organizer.class
		}
)
@SessionFactory
@TestForIssue(jiraKey = "HHH-15223")
public class EntityAsParamValueTest {

	@BeforeEach
	public void setUp(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Organizer organizer = new Organizer( 1L, "Test Organizer" );

					Event eventWithOrganizer = new Event( 1L, "Test Event", organizer );
					Event eventWithoutOrganizer = new Event( 2L, "Null Event", null );

					session.persist( organizer );
					session.persist( eventWithOrganizer );
					session.persist( eventWithoutOrganizer );
				}
		);
	}

	@Test
	public void testQueryWithEntityAsParamValue(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Organizer organizer = session.get( Organizer.class, 1L );
					assertNotNull( organizer );

					Event event = session.createQuery(
									"FROM Event e WHERE (:organizer IS NULL AND e.organizer IS NULL OR e.organizer = :organizer)",
									Event.class
							)
							.setParameter( "organizer", organizer )
							.setMaxResults( 1 )
							.uniqueResult();
					assertNotNull( event );

					event = session.createQuery(
									"FROM Event e WHERE (:organizer IS NULL AND e.organizer IS NULL OR e.organizer = :organizer)",
									Event.class
							)
							.setParameter( "organizer", null )
							.setMaxResults( 1 )
							.uniqueResult();
					assertNotNull( event );
				}
		);
	}

	@Entity(name = "Event")
	@Table(name = "EVENT_TABLE")
	public static class Event {
		@Id
		private Long id;

		private String name;

		@ManyToOne(fetch = FetchType.LAZY)
		private Organizer organizer;

		public Event() {
		}

		public Event(Long id, String name, Organizer organizer) {
			this.id = id;
			this.name = name;
			this.organizer = organizer;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Organizer getOrganizer() {
			return organizer;
		}

		public void setOrganizer(Organizer organizer) {
			this.organizer = organizer;
		}
	}

	@Entity(name = "Organizer")
	@Table(name = "ORGANIZER_TABLE")
	public static class Organizer {
		@Id
		private Long id;

		private String name;

		public Organizer() {
		}

		public Organizer(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
