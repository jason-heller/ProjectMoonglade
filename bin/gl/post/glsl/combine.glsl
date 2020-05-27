#version 150

in vec2 pass_textureCoords;

out vec4 out_color;

uniform sampler2D sampler;
uniform sampler2D highlightSampler;

void main(void){
	vec4 color = texture(sampler, pass_textureCoords);
	vec4 highlight = texture(highlightSampler, pass_textureCoords);
	
	out_color = color + highlight;
}

	
