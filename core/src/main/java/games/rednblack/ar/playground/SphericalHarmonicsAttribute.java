package games.rednblack.ar.playground;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.environment.SphericalHarmonics;

import java.util.Arrays;

public class SphericalHarmonicsAttribute extends Attribute {
    public final static String SphericalHarmonicsAlias = "ambientSphericalHarmonicsCoefficients";
    public static final long Coefficients = register(SphericalHarmonicsAlias);

    public static SphericalHarmonicsAttribute createCoefficients(SphericalHarmonics harmonics) {
        return new SphericalHarmonicsAttribute(Coefficients, harmonics);
    }

    public final SphericalHarmonics sphericalHarmonics = new SphericalHarmonics();

    protected SphericalHarmonicsAttribute(long type) {
        super(type);
    }

    protected SphericalHarmonicsAttribute(long type, SphericalHarmonics harmonics) {
        super(type);
        sphericalHarmonics.set(harmonics.data);
    }

    public SphericalHarmonicsAttribute(final SphericalHarmonicsAttribute copyFrom) {
        this(copyFrom.type, copyFrom.sphericalHarmonics);
    }

    @Override
    public Attribute copy() {
        return new SphericalHarmonicsAttribute(this);
    }

    @Override
    public int compareTo(Attribute o) {
        if (type != o.type) return type < o.type ? -1 : 1;
        return Arrays.hashCode(((SphericalHarmonicsAttribute)o).sphericalHarmonics.data) - Arrays.hashCode(sphericalHarmonics.data);
    }
}
