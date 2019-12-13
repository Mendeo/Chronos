/*����� ���� ��� ��������� ����� �������� ����� ���������� �� ListView
 * ��� ����� �� �������� ListView � �������� ��������� id ����� ��������
 * ����������, ���������� � Map'� � ������ KeyId.
 * ��� ����� ���� �������, ����� ����� ��� ����� �� �������� ListView �������� �� ������ �����
 * ������� ��������, �� � ��� �� ��� ���������� id, ����������� �������� � id � ���� ������
 * ���� id �� � ������� � ������ ������� � ������ KeyId.
 */

package ru.mendeo.chronos;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SimpleAdapter;

public class SimpleAdapterWithCustomId extends SimpleAdapter
{
	private String mKeyId;
	private List<? extends Map<String, ?>> mData;
	
	public SimpleAdapterWithCustomId(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, String keyId)
	{
		super(context, data, resource, from, to);
		mKeyId = keyId;
		mData = data;
	}
	@Override
	public long getItemId(int position)
	{
		super.getItemId(position);
		Long id = (Long)mData.get(position).get(mKeyId);
		return id.longValue();
	}
}
