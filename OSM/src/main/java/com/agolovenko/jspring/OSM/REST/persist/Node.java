package com.agolovenko.jspring.OSM.REST.persist;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="node")
@Getter
@Setter
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Node implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", nodeId=" + nodeId +
                ", lat=" + lat +
                ", lon=" + lon +
                ", tags='" + tags + '\'' +
//                ", d1=" + d1 +
//                ", d2=" + d2 +
                '}';
    }

    @Column(name="node_id")
    private Long  nodeId;

    @Column(name="lat")
    private Float lat;

    @Column(name="lon")
    private Float lon;

    @Type(type = "jsonb")
    @Column(name="tags", columnDefinition = "json") // or, jsonb
    private String tags;

//    @Formula("(select earth_distance(ll_to_earth(lat, lon), ll_to_earth(0.0,0.0)))")
//    private Float d1;
//
//    @Formula("(select earth_distance(ll_to_earth(lat, lon), ll_to_earth(0.0,90.0)))")
//    private Float d2;
}
