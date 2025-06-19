package cl.intelidata.security.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuResponse implements Comparable<MenuResponse> {

	private long idMenu;
	private String id;
	private String title;
	private String type;
	private String icon;
	private String url;
	List<MenuResponse> children;

	@Override
	public int compareTo(MenuResponse o) {
		return Long.compare(idMenu, o.getIdMenu());
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (this.getClass() != o.getClass()) {
			return false;
		} else {
			return (compareTo((MenuResponse) o) == 0);
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
