package cn.com.xpai.demo.player;

import cn.com.xpai.Option;

public class VideoFile implements Comparable<VideoFile>{

	private String name;
	private String path;
	private String pic;
	private int size;
	private String format;
	private int druration;

	public VideoFile(String name, String path, String pic, int size) {
		super();
		this.name = name;
		this.path = path;
		this.pic = pic;
		this.size = size;
	}

	public VideoFile(String name, String path, String pic, int size,
			String format, int druration) {
		super();
		this.name = name;
		this.path = path;
		this.pic = pic;
		this.size = size;
		this.format = format;
		this.druration = druration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public int getDruration() {
		return druration;
	}

	public void setDruration(int druration) {
		this.druration = druration;
	}
	
	@Override
    public int compareTo(VideoFile videoFIle) {
        if(this.name != null)
            return this.name.toLowerCase().compareTo(videoFIle.getName().toLowerCase()); 
        else 
            throw new IllegalArgumentException();
    }

}
