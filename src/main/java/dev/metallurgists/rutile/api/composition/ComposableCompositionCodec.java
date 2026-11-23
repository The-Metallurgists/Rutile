package dev.metallurgists.rutile.api.composition;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import dev.metallurgists.rutile.api.composition.element.ElementStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class ComposableCompositionCodec<T> implements Codec<T> {
	private final String name;
	private final Supplier<Codec<T>> codec;
	
	public ComposableCompositionCodec(String name, final Function<Codec<IComposable>, Codec<T>> codec) {
		this.name = name;
		this.codec = Suppliers.memoize(() -> codec.apply(MAPPING_CODEC));
	}
	
	@Override
	public <S> DataResult<Pair<T, S>> decode(final DynamicOps<S> ops, final S input) {
		return codec.get().decode(ops, input);
	}
	
	@Override
	public <S> DataResult<S> encode(final T input, final DynamicOps<S> ops, final S prefix) {
		return codec.get().encode(input, ops, prefix);
	}
	
	@Override
	public String toString() {
		return "ComposableCompositionCodec[" + name + ']';
	}
	
	private static final Codec<IComposable> MAPPING_CODEC = Codec.of(new IComposableEncoder(), new IComposableDecoder());
	
	private static class IComposableEncoder implements Encoder<IComposable> {
		@Override
		public <T> DataResult<T> encode(IComposable input, DynamicOps<T> ops, T prefix) {
			if (input instanceof ElementStack es)  {
				return ElementStack.CODEC.encodeStart(ops, es);
			}
			if (input instanceof SubComposition sc) {
				return SubComposition.CODEC.encodeStart(ops, sc);
			}
			if (input instanceof DotComposition dc) {
				return DotComposition.CODEC.encodeStart(ops, dc);
			}
			return DataResult.error(() -> "Failed to encode composition elements.");
		}
	}
	
	private static class IComposableDecoder implements Decoder<IComposable> {
		@Override
		public <T> DataResult<Pair<IComposable, T>> decode(DynamicOps<T> ops, T input) {
			var es = ElementStack.CODEC.decode(ops, input);
			if (es.isSuccess()) return es.map(this::cast);
			
			var sc = SubComposition.CODEC.decode(ops, input);
			if (sc.isSuccess()) return sc.map(this::cast);
			
			var dc = DotComposition.CODEC.decode(ops, input);
			if (dc.isSuccess()) return dc.map(this::cast);
			
			return DataResult.error(() -> "Failed to parse composition elements.");
		}
		
		public <T> Pair<IComposable, T> cast (Pair<? extends IComposable, T> pair) {
			return Pair.of((IComposable) pair.getFirst(), pair.getSecond());
		}
	}
}
