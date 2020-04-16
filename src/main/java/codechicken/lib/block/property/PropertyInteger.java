package codechicken.lib.block.property;

import com.google.common.collect.ImmutableSet;
import net.minecraft.state.Property;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by covers1624 on 5/23/2016.
 */
public class PropertyInteger extends Property<Integer> {

    private final ImmutableSet<Integer> valueSet;

    public PropertyInteger(String name, Collection<Integer> values) {
        super(name, Integer.class);
        valueSet = ImmutableSet.copyOf(values);
    }

    //EG, 16 = 0 - 15, 4 = 0 - 3
    public PropertyInteger(String name, int max) {
        super(name, Integer.class);
        ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
        for (int i = 0; i < max; i++) {
            builder.add(i);
        }
        valueSet = builder.build();
    }

    @Nonnull
    @Override
    public Collection<Integer> getAllowedValues() {
        return valueSet;
    }

    @Override
    @Nonnull
    public Optional<Integer> parseValue(@Nonnull String value) {
        try {
            Integer intValue = Integer.valueOf(value);
            if (valueSet.contains(intValue)) {
                return Optional.of(intValue);
            }
        } catch (NumberFormatException ignored) {
            // Invalid value, let it fall through
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public String getName(@Nonnull Integer value) {
        return String.valueOf(value);
    }
}
