#version 150

in vec2 pass_textureCoords;
in vec4 clipSpace;

uniform sampler2D water;
uniform sampler2D dudv;
uniform vec3 color;
uniform float timer;

out vec4 out_color;

void main(void){
	//vec2 normalizedDeviceSpace = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;

	float scroll = timer / 50.0;
	//vec2 offset = (texture(dudv, vec2(pass_textureCoords.x + scroll, pass_textureCoords.y + scroll)).rg*2.0 - 1.0) * .025;
	//normalizedDeviceSpace += offset;
	//normalizedDeviceSpace = clamp(normalizedDeviceSpace, 0.001, 0.999);

	//vec2 waterCoords = vec2(pass_textureCoords);
	//waterCoords.x += timer/10;

	out_color = vec4(color, 0.5);

	
}
