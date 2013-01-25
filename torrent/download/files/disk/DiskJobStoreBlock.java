package torrent.download.files.disk;

import torrent.TorrentException;
import torrent.download.Torrent;

public class DiskJobStoreBlock extends DiskJob {

	private int pieceIndex;
	private int blockIndex;
	private byte[] data;

	public DiskJobStoreBlock(int pieceIndex, int blockIndex, byte[] data) {
		this.pieceIndex = pieceIndex;
		this.blockIndex = blockIndex;
		this.data = data;
	}

	@Override
	public void process(Torrent torrent) {
		try {
			torrent.getFiles().getPiece(pieceIndex).storeBlock(blockIndex, data);
			if (torrent.getFiles().getPiece(pieceIndex).isDone()) {
				torrent.addToHaltingOperations(1);
				torrent.addDiskJob(new DiskJobCheckHash(pieceIndex));
			}
		} catch (TorrentException e) {
			torrent.log(e.getMessage(), true);
			torrent.getFiles().getPiece(pieceIndex).reset(blockIndex);
		}
		torrent.addToHaltingOperations(-1);
	}

	@Override
	public int getPriority() {
		return CRITICAL;
	}

}
