package dev.metallurgists.rutile.api.composition;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.metallurgists.rutile.api.composition.element.ElementStack;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.network.chat.Component;

import java.util.HashMap;

public record DotComposition(IComposable one, IComposable two) implements IComposable {
	public static final Codec<DotComposition> CODEC = new ComposableCompositionCodec<>(
		DotComposition.class.getSimpleName(),
		comp -> RecordCodecBuilder.create(i -> i.group(
			comp.fieldOf("first").forGetter(DotComposition::one),
			comp.fieldOf("second").forGetter(DotComposition::two)
		).apply(i, DotComposition::new))
	);
	
	@Override
	public int getColor() {
		return 0;
	}
	
	@Override
	public LangBuilder getDisplay() {
		return one.getDisplay()
			.add(Component.literal(" ⋅ ")) //TODO: maybe find a better dot?
			.add(two.getDisplay());
	}
	
	@Override
	public HashMap<ElementStack, Integer> getContainedElements() {
		return null;
	}
	
	@Override
	public JsonObject toJson() {
		return serialize(CODEC, this);
	}
	
	//TODO: Builder stuff
}
