#version 150

in vec2 pass_textureCoords;

out vec4 out_color;

uniform sampler2D sampler;

void main(void){

	out_color = texture(sampler, pass_textureCoords);
}
