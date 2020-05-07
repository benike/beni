package hu.beni.amusementpark.entity;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;

@Entity
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = { "id", "textOfRegistry", "dateOfRegistry" })
public class GuestBookRegistry implements Serializable {

	private static final long serialVersionUID = 2987327348565883455L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@NotNull
	@Size(min = 2, max = 100)
	private String textOfRegistry;

	@CreationTimestamp
	private LocalDateTime dateOfRegistry;

	@NotNull
	@ManyToOne(fetch = LAZY)
	private Visitor visitor;

	@NotNull
	@ManyToOne(fetch = LAZY)
	private AmusementPark amusementPark;

	@Tolerate
	protected GuestBookRegistry() {
		super();
	}

}
