package index.alchemy.util;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public interface DeobfuscatingRemapper {
	
	DeobfuscatingRemapper INSTANCE = new DeobfuscatingRemapper() {
		
		@Override
		public String unmapType(String typeName) {
			return FMLDeobfuscatingRemapper.INSTANCE.unmap(typeName);
		}
		
		@Override
		public String mapType(String typeName) {
			return FMLDeobfuscatingRemapper.INSTANCE.map(typeName);
		}
		
		@Override
		public String mapMethodName(String owner, String name, String desc) {
			return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner.replace('.', '/'), name, desc);
		}
		
		@Override
		public String mapFieldName(String owner, String name, String desc) {
			return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner.replace('.', '/'), name, desc);
		}
		
	};

	String unmapType(String typeName);
	
	String mapType(String typeName);
	
	String mapMethodName(String owner, String name, String desc);
	
	String mapFieldName(String owner, String name, String desc);

}