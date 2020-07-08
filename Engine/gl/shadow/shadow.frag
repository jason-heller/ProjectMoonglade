#version 330

in vec2 pass_uvs;
out vec4 out_colour;

uniform sampler2D diffuse;

void main(void){
	float alpha = texture(diffuse, pass_uvs).a;
	
	if (alpha < 0.5)
		discard;
	
	out_colour = vec4(1.0);
	
}