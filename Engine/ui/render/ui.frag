#version 150

in vec2 pass_uvs;

out vec4 out_color;

uniform vec3 color;
uniform float opacity;
uniform sampler2D fontAtlas;

void main(void){

	vec4 tex = texture(fontAtlas, pass_uvs);

	out_color = vec4(tex.rgb * color.rgb, tex.a * opacity);

}
