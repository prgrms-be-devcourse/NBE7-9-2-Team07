package com.back.pinco.global.geometry;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeometryUtil {

    private final GeometryFactory geometryFactory;

    /**
     * 경도와 위도로 Point 객체를 생성
     *
     * @param longitude 경도 (Longitude)
     * @param latitude  위도 (Latitude)
     * @return Point 객체
     */
    public Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    /** Point 객체에서 경도를 추출 */
    public double getLongitude(Point point) {
        return point.getX();
    }

    /** Point 객체에서 위도를 추출 */
    public double getLatitude(Point point) {
        return point.getY();
    }

    /** 두 지점 간의 거리를 계산(단위: 미터) */
    public double distance(Point point1, Point point2) {
        return point1.distance(point2) * 111320; // 대략적인 미터 변환
    }
}
