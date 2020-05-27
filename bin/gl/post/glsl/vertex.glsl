#version 150

in vec2 in_position;
in vec2 in_textureCoords;

out vec2 pass_textureCoords;

void main(void){

	gl_Position = vec4(in_position*2.0, 0.0, 1.0);
	
	pass_textureCoords = in_textureCoords;
}
